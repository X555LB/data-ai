# 前端开发规范

## 目录

1. [系统组件库](#1-系统组件库)
2. [通用 Hooks](#2-通用-hooks)
3. [CRUD 开发模式](#3-crud-开发模式)
4. [国际化](#4-国际化)
5. [调试与格式化](#5-调试与格式化)

---

## 1. 系统组件库

项目内置丰富的业务组件，**优先复用**：

| 组件名 | 路径 | 描述 | 使用场景 |
|--------|------|------|----------|
| **Editor** | `components/Editor` | 富文本编辑器 (WangEditor) | 公告内容、文章编辑 |
| **Dialog** | `components/Dialog` | 增强版 ElDialog | 表单弹窗、详情弹窗 |
| **ContentWrap** | `components/ContentWrap` | 内容包裹器 (ElCard 封装) | 列表页、详情页容器 |
| **UploadFile** | `components/UploadFile` | 文件上传 | 上传附件、文档 |
| **UploadImg** | `components/UploadFile` | 图片上传 | 上传头像、商品图 |
| **Pagination** | `components/Pagination` | 分页组件 | 列表页底部分页条 |
| **EChart** | `components/EChart` | 图表组件 | 数据统计、大屏展示 |

### 使用示例
```vue
<template>
  <ContentWrap>
    <el-table :data="list">...</el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>
```

---

## 2. 通用 Hooks

### useMessage - 交互反馈

位置: `hooks/web/useMessage.ts`

```typescript
import { useMessage } from '@/hooks/web/useMessage'

const { message, delConfirm } = useMessage()

// 消息提示
message.success('操作成功')
message.error('操作失败')

// 删除确认框
await delConfirm()  // 自动弹出"是否删除"确认框
// 用户点击确认后继续执行
```

### useCache - 缓存管理

位置: `hooks/web/useCache.ts`

```typescript
import { useCache } from '@/hooks/web/useCache'

const { wsCache } = useCache()

wsCache.set('key', value)
wsCache.get('key')
```

### download - 下载工具

位置: `utils/download.ts`

```typescript
import { download } from '@/utils/download'

download.excel(data, '文件名.xls')
```

---

## 3. CRUD 开发模式

### 模式一：原生 Element Plus (推荐)

适用于 90% 的业务场景，灵活性高。

**结构**: `Search` (Form) + `Table` + `Pagination` + `Dialog` (Form)

```vue
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserPage } from '@/api/system/user'

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  username: undefined,
  status: undefined
})
const list = ref([])
const total = ref(0)
const loading = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

onMounted(() => getList())
</script>

<template>
  <ContentWrap>
    <!-- 搜索表单 -->
    <el-form :model="queryParams" inline>
      <el-form-item label="用户名">
        <el-input v-model="queryParams.username" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="getList">搜索</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="status" label="状态" />
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>
```

### 模式二：CRUD 组件 (`useCrudSchemas`)

适用于极简单的单表 CRUD，字段少，无复杂交互。

```typescript
import { useCrudSchemas, CrudSchema } from '@/hooks/web/useCrudSchemas'

const crudSchemas = reactive<CrudSchema[]>([
  { field: 'title', label: '标题', search: { show: true } },
  { field: 'status', label: '状态', form: { component: 'Select' } }
])

const { allSchemas } = useCrudSchemas(crudSchemas)
```

配合 `<Search>`, `<Table>`, `<Form>` 组件渲染 `allSchemas`。

---

## 4. 国际化

### 使用规范

1. **定义**: 在 `src/locales/zh-CN.ts` 添加 Key-Value
2. **使用**:
```typescript
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

// ❌ 错误
const title = '系统管理'

// ✅ 正确
const title = t('system.management')
```

3. **动态切换**: `useLocale().changeLocale('en')`

---

## 5. 调试与格式化

### 代码规范

- **工具**: ESLint + Prettier
- **格式**: 单引号、无分号、尾随逗号（参考 `.prettierrc`）
- **自动修复**: `npm run lint:eslint`

### IDE 调试

- **VS Code**: 使用 "Launch Chrome" 配置
- **断点**: 可在 `.ts` / `.vue` 文件中直接打断点调试

---

## API 定义规范

```typescript
// src/api/system/user/index.ts
import request from '@/config/axios'

export interface UserPageReqVO {
  pageNo: number
  pageSize: number
  username?: string
  status?: number
}

export const getUserPage = (params: UserPageReqVO) => {
  return request.get({ url: '/system/user/page', params })
}

export const createUser = (data: UserCreateReqVO) => {
  return request.post({ url: '/system/user/create', data })
}

export const updateUser = (data: UserUpdateReqVO) => {
  return request.put({ url: '/system/user/update', data })
}

export const deleteUser = (id: number) => {
  return request.delete({ url: `/system/user/delete?id=${id}` })
}
```

---

## 目录映射

前端目录与后端模块对应：

| 后端模块 | 前端 Views | 前端 API |
|----------|------------|----------|
| `yudao-module-system` | `src/views/system` | `src/api/system` |
| `yudao-module-infra` | `src/views/infra` | `src/api/infra` |
| `yudao-module-bpm` | `src/views/bpm` | `src/api/bpm` |
