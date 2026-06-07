target/
dist/
# make_public_copy.ps1
# 在项目根目录运行此脚本以创建一个脱敏的公开副本（不会修改原目录）
# 使用方法：在项目根打开 PowerShell（非必须管理员），然后运行：
#   .\make_public_copy.ps1

$src = (Get-Location).Path
$baseName = Split-Path $src -Leaf
$parent = Split-Path $src -Parent
$dest = Join-Path $parent ($baseName + "-public")

Write-Host "源目录: $src"
Write-Host "目标目录: $dest"

if (Test-Path $dest) {
    Write-Host "目标目录已存在，将先删除旧目录: $dest"
    Remove-Item -LiteralPath $dest -Recurse -Force
}

# 要排除的相对目录（基于源目录）
$excludeDirs = @('.git','.idea','node_modules','backend\target','backend\bin','target','dist')

# 构建 robocopy 的排除参数（使用完整路径）
$excludeFull = $excludeDirs | ForEach-Object { Join-Path $src $_ }
$excludeArgs = "/XD " + ($excludeFull | ForEach-Object { '"' + $_ + '"' } ) -join ' '

Write-Host "开始复制文件（使用 robocopy，若无 robocopy 将退回到 Copy-Item）..."

$robocopyCmd = "robocopy `"$src`" `"$dest`" /E /COPYALL /R:1 /W:1 $excludeArgs"
try {
    Invoke-Expression $robocopyCmd | Out-Null
} catch {
    Write-Host "robocopy 执行异常，退回到 PowerShell 复制。"
}

if (-not (Test-Path $dest)) {
    Write-Host "robocopy 未能创建目标目录，使用 Copy-Item 进行复制（速度较慢）。"
    # 使用递归复制并跳过常见大目录
    $items = Get-ChildItem -Path $src -Force
    New-Item -ItemType Directory -Path $dest | Out-Null
    foreach ($item in $items) {
        if ($excludeDirs -contains $item.Name) { continue }
        $target = Join-Path $dest $item.Name
        Copy-Item -Path $item.FullName -Destination $target -Recurse -Force -ErrorAction SilentlyContinue
    }
}

# 创建 .gitignore
$gitignore = @'
# Build
target/
backend/target/
dist/
node_modules/

# IDE
.idea/
.vscode/
*.iml

# Compiled
*.class
*.jar

# OS
.DS_Store
Thumbs.db

# Secrets
.env
*.jks
*.keystore
*.p12
'@

Set-Content -Path (Join-Path $dest '.gitignore') -Value $gitignore -Encoding UTF8

# 脱敏 application.properties
$appPropsPath = Join-Path $dest 'backend\src\main\resources\application.properties'
if (Test-Path $appPropsPath) {
    $text = Get-Content $appPropsPath -Raw
    $text = $text -replace 'spring\.datasource\.username=.*', 'spring.datasource.username=${JDBC_DATABASE_USERNAME:}'
    $text = $text -replace 'spring\.datasource\.password=.*', 'spring.datasource.password=${JDBC_DATABASE_PASSWORD:}'
    $text = $text -replace 'spring\.datasource\.url=.*', 'spring.datasource.url=${JDBC_DATABASE_URL:jdbc:mysql://localhost:3306/unicourse?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}'
    Set-Content -Path $appPropsPath -Value $text -Encoding UTF8
    Write-Host "已脱敏: $appPropsPath"
} else {
    Write-Host "未找到 application.properties，跳过该文件的脱敏。"
}

# 脱敏 WORKFLOW.md 中的示例凭据
$workflowPath = Join-Path $dest 'WORKFLOW.md'
if (Test-Path $workflowPath) {
    $text = Get-Content $workflowPath -Raw
    $text = $text -replace 'unicourse_user', '<DB_USERNAME>'
    $text = $text -replace 'change_me', '<DB_PASSWORD>'
    Set-Content -Path $workflowPath -Value $text -Encoding UTF8
    Write-Host "已脱敏: $workflowPath"
}

# 删除可能被复制过来的二进制文件
Get-ChildItem -Path $dest -Include *.class,*.jar -Recurse -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue

Write-Host "公开副本已生成: $dest"
Write-Host "请手动检查副本以确认敏感信息已被替换占位符。"
Write-Host '初始化 Git 仓库并推送示例（在目标目录中分别运行以下命令）：'
Write-Host '  cd "' + $dest + '"'
Write-Host '  git init'
Write-Host '  git add .'
Write-Host '  git commit -m "Initial public sanitized import"'
Write-Host '使用 gh CLI 创建仓库示例（替换为你的用户/组织和仓库名）：'
Write-Host '  gh repo create your-user-or-org/your-repo-name --public --source=. --remote=origin'
Write-Host '  git push -u origin main'

Write-Host "完成。"
