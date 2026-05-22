#!/usr/bin/env bash
# setup-shared-rules.sh
# 以 .agents 为主源，将 rules / skills 复制到各 AI 工具目录
# 目标工具：CodeBuddy / Qoder / Trae
# 使用方法: bash setup-shared-rules.sh

set -euo pipefail

# 切换到脚本所在目录（即项目根目录），避免相对路径出错
cd "$(dirname "$0")"

AGENTS_DIR=".agents"
RULES_SRC="$AGENTS_DIR/rules"
SKILLS_SRC="$AGENTS_DIR/skills"

# rules 同步目标目录
RULES_TARGETS=(
  ".codebuddy/rules"
  ".qoder/rules"
  ".trae/rules"
)

# skills 同步目标目录
SKILLS_TARGETS=(
  ".codebuddy/skills"
  ".qoder/skills"
  ".trae/skills"
)

# 将 src 目录内容完整复制到 dst（以 src 为准，先清空 dst）
sync_dir() {
  local src="$1"
  local dst="$2"

  if [ ! -d "$src" ]; then
    echo "⚠️  源目录不存在，跳过: $src"
    return
  fi

  # 清空目标目录（保留目录本身），再从源全量复制
  rm -rf "$dst"
  mkdir -p "$dst"

  if [ -n "$(ls -A "$src" 2>/dev/null)" ]; then
    cp -r "$src"/. "$dst"/
    local count
    count=$(find "$dst" -type f 2>/dev/null | wc -l | tr -d ' ')
    echo "📋 已同步: $src → $dst（$count 个文件）"
  else
    echo "ℹ️  源目录为空: $src"
  fi
}

echo "🚀 开始同步，以 $AGENTS_DIR 为主源..."
echo ""

echo "📁 同步 rules..."
for dst in "${RULES_TARGETS[@]}"; do
  sync_dir "$RULES_SRC" "$dst"
done

echo ""
echo "📁 同步 skills..."
for dst in "${SKILLS_TARGETS[@]}"; do
  sync_dir "$SKILLS_SRC" "$dst"
done

echo ""
echo "✅ 同步完成"