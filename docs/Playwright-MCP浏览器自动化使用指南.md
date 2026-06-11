# Playwright MCP 浏览器自动化使用指南

## 1. 前置准备

### 1.1 安装 Chromium 浏览器

Playwright MCP 首次使用前**必须安装** Chrome/Chromium：

```bash
npx playwright install chrome
```

> **注意**：该命令不支持 `--yes` 参数，直接运行即可。

### 1.2 工具 Schema 必须先读取

调用任何 MCP 工具前，**必须先读取该工具的 JSON schema 文件**，确认参数名称和类型，不可猜测。

Schema 路径：`C:\Users\<user>\AppData\Roaming\Qoder\SharedClientCache\mcps\playwright\tools\<tool_name>.json`

## 2. 核心工具与使用流程

### 2.1 工具调用顺序

| 步骤 | 工具 | 用途 |
|------|------|------|
| 1 | `browser_navigate` | 导航到目标 URL |
| 2 | `browser_snapshot` | 获取页面可访问性快照（DOM 结构树，含 ref 引用） |
| 3 | `browser_fill_form` | 填写表单字段（需指定 target ref） |
| 4 | `browser_click` | 点击按钮/链接（需指定 target ref） |
| 5 | `browser_take_screenshot` | 截图保存（用于结果验证） |
| 6 | `browser_run_code_unsafe` | 执行自定义 Playwright 代码（高级操作） |

### 2.2 关键概念：ref 引用

`browser_snapshot` 返回的 YAML 结构中，每个元素都有 `ref` 属性（如 `ref=e18`）。后续交互工具（click、fill_form 等）通过 `target` 参数引用这个 ref 来定位元素。

**典型工作流**：snapshot → 找到目标元素的 ref → click/fill 使用该 ref

### 2.3 表单填写示例

```json
{
  "fields": [
    { "target": "e18", "name": "账号", "type": "textbox", "value": "admin" },
    { "target": "e25", "name": "密码", "type": "textbox", "value": "123456" }
  ]
}
```

### 2.4 按钮点击示例

```json
{ "target": "e30", "element": "登录按钮" }
```

## 3. 滑块验证码绕过方案

### 3.1 问题

登录页面使用了**拼图型滑块验证码**（`SlideVerify` 组件），需要精确匹配像素位置，自动化拖动成功率极低。

### 3.2 尝试过的方法（均失败）

- `browser_drag`：拖到错误位置
- `browser_run_code_unsafe` + mouse.move 模拟：位置精度不够，或 trail 检测不通过

### 3.3 最终方案：通过 Vue 组件内部绕过（成功）

利用 `browser_run_code_unsafe` 直接调用 Vue 组件的方法：

```javascript
async (page) => {
  await page.evaluate(() => {
    // 1. 获取 Vue 根实例
    const app = document.querySelector('#app').__vue__;
    // 2. 找到 Login 组件（Vue 组件树遍历）
    const login = app.$children[0];
    // 3. 设置表单数据
    login.loginForm.username = 'admin';
    login.loginForm.password = '123456';
    // 4. 直接调用验证码成功后的回调方法
    login.captchaChoose();
  });
  await page.waitForTimeout(5000);
}
```

**核心思路**：
1. 通过 `document.getElementById('slideVerify').__vue__` 获取滑块组件实例
2. 沿组件树向上查找（`$parent`），定位到 `Login` 组件
3. 查看 `Login` 组件的 methods（`captchaChoose` 是验证码成功后的回调）
4. 直接调用 `captchaChoose()` 跳过验证码

### 3.4 Vue 组件树探查技巧

```javascript
// 遍历组件链
let comp = document.getElementById('slideVerify').__vue__;
while (comp) {
  console.log(comp.$options.name, Object.keys(comp.$options.methods || {}));
  comp = comp.$parent;
}

// 查看组件数据
comp.$data
// 查看组件事件
Object.keys(comp._events || {})
```

## 4. 高级操作：browser_run_code_unsafe

### 4.1 适用场景

- 需要精确的鼠标控制（如拖拽）
- 需要直接操作 DOM 或 Vue 实例
- 需要执行复杂的页面交互逻辑
- 需要读取页面 JavaScript 变量

### 4.2 注意事项

- **不可使用 `setTimeout`**，用 `page.waitForTimeout(ms)` 代替
- **不可序列化 Vue 实例**等复杂对象，只返回基本类型或简单 JSON
- 代码必须是 `async (page) => { ... }` 格式的函数

### 4.3 查看网络请求

```
browser_network_requests → 列出所有请求（带编号）
browser_network_request → 用编号查看具体请求的详情（headers/body/response）
```

## 5. 本次测试路径记录

```
登录页 (localhost:9526/login)
  ├── 填写 admin / 123456
  ├── 绕过滑块验证码（captchaChoose）
  └── 跳转到 估值记录 (/valuation/project/catalog)
       ├── 点击 "执行估值"（信用评估记录）
       ├── 进入估值流程页面
       │    ├── 市场法估值（已完成）→ "修改估值"
       │    ├── 成本法估值（已完成）→ "修改估值"
       │    └── 收益法估值（估值中）→ "继续估值"
       └── 点击 市场法估值的"修改估值"
            └── 第1步 搜寻市场价格
                 ├── "智能体寻价" 按钮 ← 测试目标
                 └── "手动选择" 按钮
```

## 6. 测试结果

- **按钮名称**：智能体寻价（注意是"寻"不是"询"）
- **按钮行为**：点击后调用后端 AI 智能体匹配市场价格知识库
- **返回结果**：弹出提示 "未找到匹配的市场价格知识库，是否需要手动选择？"
- **对话框选项**：取消 / 手动匹配
- **结论**：前后端链路正常，缺少市场价格知识库数据导致返回空结果
