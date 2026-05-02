# 工作流/BPM 开发规范

## 目录

1. [架构概览](#1-架构概览)
2. [模块初始化](#2-模块初始化)
3. [接入模式](#3-接入模式)
4. [业务表单开发](#4-业务表单开发)
5. [任务分配策略](#5-任务分配策略)
6. [多人审批模式](#6-多人审批模式)
7. [流程实例生命周期](#7-流程实例生命周期)
8. [任务管理](#8-任务管理)
9. [监听器开发](#9-监听器开发)
10. [流程表达式](#10-流程表达式)

---

## 1. 架构概览

基于 **Flowable 7.0** 深度封装，提供符合中国企业需求的审批流功能。

### 核心组件
- **模块**: `yudao-module-bpm`
- **引擎**: Flowable (BPMN 2.0 标准)
- **设计器**:
  - **BPMN**: 基于 `bpmn-js`，适合复杂逻辑编排
  - **Simple**: 仿钉钉/飞书 UI，适合业务人员

### 数据层次
| 层次 | 说明 | 存储位置 |
|------|------|----------|
| 流程模型 (Model) | 设计草稿 | `bpm_model` |
| 流程定义 (Definition) | 发布后的版本 | `ACT_RE_PROCDEF` |
| 流程实例 (Instance) | 运行中的流程 | `ACT_RU_EXECUTION` |

---

## 2. 模块初始化

BPM 模块默认关闭，需手动开启：

### 步骤
1. **POM 开启**:
   - 根目录 `pom.xml`: 解除 `yudao-module-bpm` 注释
   - `yudao-server/pom.xml`: 解除依赖注释

2. **SQL 导入**: 导入 `bpm.sql` (含 `bpm_` 前缀表)
   - Flowable 引擎表 (`ACT_`, `FLW_`) 启动时自动创建

### 常见报错

| 错误 | 解决方案 |
|------|----------|
| `max key length is 1000 bytes` | 设置 MySQL `default-storage-engine=innodb` |
| `problem during schema upgrade` | MySQL 需配置为大小写敏感 |

---

## 3. 接入模式

### 模式一：流程表单 (Process Form)

**适用场景**: 简单行政审批（请假、报销），无需开发代码

- 在线拖拽生成表单 JSON
- 数据存储在 Flowable 流程变量中
- 只需在管理后台操作

### 模式二：业务表单 (Business Form) ⭐

**适用场景**: 复杂业务（合同审批、采购入库），需独立业务表

**开发流程**:
1. **建表**: 必须包含 `process_instance_id` 和 `status` 字段
2. **后端**: Controller 调用流程 API，Listener 监听状态
3. **前端**: 创建 create.vue 和 detail.vue 页面
4. **路由配置**: 在流程模型中配置表单路由

---

## 4. 业务表单开发

### 后端：发起流程

```java
@Service
public class OALeaveServiceImpl implements OALeaveService {
    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLeave(OALeaveCreateReqVO reqVO) {
        // 1. 插入业务数据
        OALeaveDO leave = OALeaveConvert.INSTANCE.convert(reqVO);
        leave.setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        oaLeaveMapper.insert(leave);

        // 2. 发起流程
        String processInstanceId = processInstanceApi.createProcessInstance(
            SecurityFrameworkUtils.getLoginUserId(),
            new BpmProcessInstanceCreateReqVO()
                .setProcessDefinitionKey("oa_leave")      // 流程标识
                .setBusinessKey(leave.getId().toString()) // 业务键
                .setVariables(MapUtil.of("day", leave.getDay()))
        );

        // 3. 回填实例ID
        oaLeaveMapper.updateById(new OALeaveDO()
            .setId(leave.getId())
            .setProcessInstanceId(processInstanceId));
        return leave.getId();
    }
}
```

### 后端：状态监听

```java
@Component
public class OALeaveStatusListener extends BpmProcessInstanceStatusEventListener {
    @Resource
    private OALeaveService oaLeaveService;

    @Override
    protected String getProcessDefinitionKey() {
        return "oa_leave";
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        oaLeaveService.updateLeaveStatus(
            Long.parseLong(event.getBusinessKey()), 
            event.getStatus()
        );
    }
}
```

### 前端：路由配置

在流程模型设计器中配置：
- **表单提交路由**: `/bpm/oa/leave/create`
- **表单查看路由**: `/bpm/oa/leave/detail`

---

## 5. 任务分配策略

### 内置策略

| 策略 | 类名 | 说明 |
|------|------|------|
| 角色 | `BpmTaskCandidateRoleStrategy` | 指定角色 |
| 部门成员 | `BpmTaskCandidateDeptMemberStrategy` | 部门下所有成员 |
| 部门负责人 | `BpmTaskCandidateDeptLeaderStrategy` | 部门负责人 |
| 岗位 | `BpmTaskCandidatePostStrategy` | 指定岗位 |
| 用户 | `BpmTaskCandidateUserStrategy` | 指定用户 |
| 发起人自选 | `BpmTaskCandidateStartUserSelectStrategy` | 运行时动态指定 |

### 自定义策略

```java
@Component
public class AmountBasedStrategy implements BpmTaskCandidateStrategy {
    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.AMOUNT_BASED;
    }

    @Override
    public Set<Long> calculateUsers(DelegateExecution execution, String param) {
        Long amount = (Long) execution.getVariable("amount");
        if (amount > 10000) {
            return Set.of(1L); // 总经理
        }
        return Set.of(2L); // 经理
    }
}
```

---

## 6. 多人审批模式

| 模式 | 说明 | Flowable 表达式 |
|------|------|-----------------|
| **会签 (AND)** | 所有人通过才通过 | `${nrOfCompletedInstances >= nrOfInstances}` |
| **或签 (OR)** | 任意一人通过即可 | `${nrOfCompletedInstances == 1}` |
| **依次审批** | 按顺序串行审批 | `isSequential=true` |
| **票签** | 超过50%通过 | `${nrOfCompletedInstances / nrOfInstances > 0.5}` |

---

## 7. 流程实例生命周期

### 状态管理

状态存储在流程变量 `PROCESS_STATUS` 中：
- `RUNNING` - 进行中
- `FINISH` - 完成
- `CANCEL` - 取消
- `REJECT` - 不通过

### 核心操作

| 操作 | API | 底层 |
|------|-----|------|
| 发起流程 | `createProcessInstance` | `RuntimeService.start()` |
| 取消流程 | `cancelProcessInstance` | `RuntimeService.deleteProcessInstance()` |
| 我的流程 | 查询 `ACT_HI_PROCINST` | 历史表（流程结束后运行时表清空） |

---

## 8. 任务管理

### 任务查询

- **待办任务**: 查询 `ACT_RU_TASK`
- **已办任务**: 查询 `ACT_HI_TASKINST`

### 审批动作

| 动作 | 方法 | 效果 |
|------|------|------|
| **通过** | `approveTask` | 任务完成，流程继续 |
| **不通过** | `rejectTask` | 整个流程结束 (一票否决) |
| **驳回/退回** | `returnTask` | 回退到指定历史节点 |

### 任务流转

| 操作 | 方法 | 说明 |
|------|------|------|
| **转办** | `transferTask` | 永久移交所有权 |
| **委派** | `delegateTask` | 临时移交，需归还 |
| **抄送** | - | 仅通知，不阻断流程 |

### 加签

```java
// 向前加签
createSignTask(reqVO)  // scopeType=before

// 向后加签
createSignTask(reqVO)  // scopeType=after
```

---

## 9. 监听器开发

### 监听器类型

1. **ExecutionListener**: 监听 Start/End 事件
2. **TaskListener**: 监听 Create/Assignment/Complete/Delete

### 开发规范

使用 **Spring Bean** 模式：

```java
@Component("myTaskListener")
public class MyTaskListener implements TaskListener {
    @Resource
    private UserService userService;

    @Override
    public void notify(DelegateTask delegateTask) {
        if ("create".equals(delegateTask.getEventName())) {
            String assignee = delegateTask.getAssignee();
            userService.sendNotification(assignee, "您有新任务");
        }
    }
}
```

**配置**: 在 BPMN 设计器中选择"委托表达式"，填写 `${myTaskListener}`

---

## 10. 流程表达式

### 内置表达式

```
// 分配给发起人
${bpmTaskAssignStartUserExpression.calculateUsers(execution)}

// 分配给发起人的 Leader
${bpmTaskAssignLeaderExpression.calculateUsers(execution, 1)}  // 1=直接主管
${bpmTaskAssignLeaderExpression.calculateUsers(execution, 2)}  // 2=二级主管
```

---

## 开发注意事项

> **禁止**在 Controller 层直接调用 Flowable 原生 API (`RuntimeService`, `TaskService`)

必须使用 data-ai 封装的 Service：
- `BpmProcessInstanceService.createProcessInstance()`
- `BpmTaskService.approveTask()`
- `BpmTaskService.rejectTask()`
