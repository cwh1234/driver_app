# DriverTest - 驾考知识卡片学习助手 📚

一款基于 Android 的驾考知识卡片学习应用，帮助你通过碎片化复习轻松备考驾照理论考试。支持手动输入、拍照 OCR 识别和 AI 智能生成三种方式创建知识卡片，采用间隔复习法帮你高效记忆。

## ✨ 功能特性

### 📝 三种添加方式
- **文本输入** — 手动输入标题和内容，快速创建知识卡片
- **拍照 OCR** — 拍照或从相册选择图片，自动识别中文文字并转换为卡片
- **AI 搜索** — 输入主题关键词，由 DeepSeek AI 自动生成结构化的知识卡片

### 📖 智能学习
- 翻转卡片式浏览体验
- 三档掌握度标记：不熟悉 / 模糊 / 掌握
- 基于掌握程度自动调整复习频率
- 每日复习任务自动管理

### 📊 学习统计
- 总卡片数、掌握率、今日复习量、连续学习天数概览
- 每日学习进度柱状图
- 每张卡片的详细复习统计

### ⚙️ 个性化设置
- 自定义 DeepSeek API Key
- 复习提醒通知
- 数据管理

## 📸 截图

> 即将更新

## 🛠 技术栈

| 类别 | 技术 |
|------|------|
| **语言** | Kotlin |
| **UI 框架** | Jetpack Compose + Material 3 |
| **架构** | MVVM + Repository Pattern |
| **依赖注入** | Hilt |
| **本地数据库** | Room |
| **网络请求** | Retrofit + OkHttp |
| **OCR** | Google ML Kit (中文识别) |
| **摄像头** | CameraX |
| **图片加载** | Coil |
| **导航** | Navigation Compose |
| **JSON** | Gson |

## 🏗 项目结构

```
app/src/main/java/com/drivertest/app/
├── data/
│   ├── local/          # Room 数据库、DAO、实体
│   ├── remote/         # DeepSeek API 服务、DTO、拦截器
│   └── repository/     # 数据仓库层
├── di/                 # Hilt 依赖注入模块
├── domain/model/       # 领域模型
├── ocr/                # ML Kit OCR 处理
├── ui/
│   ├── add/            # 添加卡片页 (文本/OCR/AI)
│   ├── components/     # 通用 UI 组件
│   ├── learn/          # 学习/复习页
│   ├── navigation/     # 导航图
│   ├── settings/       # 设置页
│   ├── stats/          # 统计页
│   └── theme/          # Material 3 主题
└── util/               # 工具类
```

## 🚀 构建运行

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Gradle 8.5+
- Android SDK 34

### 构建步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/cwh1234/driver_app.git
   cd driver_app
   ```

2. **使用 Android Studio 打开项目**
   - File → Open → 选择 `driver_app` 目录
   - 等待 Gradle 同步完成

3. **配置 API Key（可选）**
   - 在应用内「设置」页面输入你的 DeepSeek API Key
   - 或通过环境变量 `DEEPSEEK_API_KEY` 配置

4. **构建 APK**
   ```bash
   # Debug 版本
   ./gradlew assembleDebug

   # Release 版本
   ./gradlew assembleRelease
   ```

5. **安装运行**
   ```bash
   ./gradlew installDebug
   ```

   或直接在 Android Studio 中点击 Run ▶️

## 📱 系统要求

- Android 8.0 (API 26) 及以上
- 需要网络连接（AI 搜索功能）
- 摄像头权限（拍照 OCR 功能，可选）

## 📦 APK 下载

前往.\app\build\outputs\apk\debug，下载.apk文件，可以直接在 Android 设备上安装使用。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

---

**DriverTest** — 让驾考复习更高效 🚗
