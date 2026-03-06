# 东方Project游戏管理器 (Touhou Manager)

一个基于 Vue 3 + Spring Boot 的东方Project游戏管理工具，支持游戏启动、Replay 管理和统计功能。

## 项目结构

```
TouHouManager/
├── src/main/java/           # Spring Boot 后端
│   └── com/thmanager/
│       ├── controller/      # API 控制器
│       ├── service/         # 业务逻辑
│       ├── dao/             # 数据访问
│       ├── model/           # 实体类
│       ├── config/          # 配置类
│       └── websocket/       # WebSocket 服务
├── src/main/resources/      # 后端资源
│   ├── static/              # 前端构建输出
│   └── database/            # 数据库脚本
├── thmanager-frontend/      # Vue 3 前端
│   ├── src/
│   │   ├── api/             # API 接口
│   │   ├── components/      # 组件
│   │   ├── views/           # 页面
│   │   ├── stores/          # Pinia 状态管理
│   │   └── router/          # 路由配置
│   └── package.json
└── pom.xml                  # Maven 配置
```

## 技术栈

### 后端
- **Spring Boot 3.2.0** - 主框架
- **Spring Web** - REST API
- **Spring WebSocket** - 实时通知
- **SQLite** - 数据库
- **Gson** - JSON 处理

### 前端
- **Vue 3** - 前端框架
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Element Plus** - UI 组件库
- **ECharts** - 图表库
- **Axios** - HTTP 客户端

## 环境要求

- **JDK 21** 或更高版本
- **Node.js 20** 或更高版本
- **Maven 3.8** 或更高版本

## 快速开始

### 方法一：开发模式（推荐）

#### 1. 启动后端服务

```bash
# 在项目根目录执行
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

#### 2. 启动前端开发服务器

```bash
# 进入前端目录
cd thmanager-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器将在 http://localhost:5173 启动

#### 3. 访问应用

打开浏览器访问 http://localhost:5173

### 方法二：生产模式

#### 1. 构建完整项目

```bash
# 在项目根目录执行，使用 prod profile 构建
mvn clean package -P prod
```

这将：
- 构建 Vue 前端
- 将前端资源复制到 Spring Boot 的 static 目录
- 打包成可执行的 JAR 文件

#### 2. 运行生产版本

```bash
# 运行打包后的 JAR
java -jar target/thmanager-1.0-SNAPSHOT.jar
```

然后访问 http://localhost:8080

## API 接口

### 游戏管理
- `GET /api/games` - 获取所有游戏
- `GET /api/games/{id}` - 获取单个游戏
- `PUT /api/games/{id}` - 更新游戏信息
- `POST /api/games/{id}/launch` - 启动游戏
- `POST /api/games/stop` - 停止游戏
- `GET /api/games/status` - 获取游戏运行状态

### Replay 管理
- `GET /api/replays` - 获取所有 Replay
- `GET /api/replays/game/{gameId}` - 获取指定游戏的 Replay
- `GET /api/replays/{id}` - 获取单个 Replay
- `DELETE /api/replays/{id}` - 删除 Replay
- `POST /api/replays/scan` - 扫描新 Replay

### 统计
- `GET /api/stats` - 获取总体统计
- `GET /api/stats/game/{gameId}` - 获取游戏统计
- `GET /api/stats/playtime` - 获取游戏时长统计
- `GET /api/stats/scores` - 获取分数统计

### 设置
- `GET /api/settings/games` - 获取所有游戏设置
- `PUT /api/settings/games/{id}` - 更新游戏设置
- `POST /api/settings/batch-update` - 批量更新路径

## 功能特性

### 已实现
- ✅ 游戏库管理 - 查看已安装的东方Project游戏
- ✅ 游戏启动 - 支持倒计时启动
- ✅ Replay 管理 - 自动扫描、查看、删除 Replay
- ✅ 数据统计 - 游戏时长、分数、通关率等统计
- ✅ 实时通知 - WebSocket 实时推送游戏状态
- ✅ 路径设置 - 批量设置游戏安装路径

### 待实现
- 📝 Replay 分析详情
- 📝 更多统计图表
- 📝 成就系统
- 📝 数据备份/恢复
- 📝 玩家聊天系统
- 📝 玩家排行榜系统

## 配置说明

### 后端配置 (application.properties)

```properties
# 服务器端口
server.port=8080

# CORS 配置
spring.web.cors.allowed-origins=http://localhost:5173

# 数据库位置
spring.datasource.url=jdbc:sqlite:${user.home}/.touhou-manager/touhou_manager.db
```

### 前端配置 (vite.config.js)

```javascript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 常见问题

### 1. 前端安装依赖失败

```bash
# 使用淘宝镜像
npm config set registry https://registry.npmmirror.com
npm install
```

### 2. 后端启动报错

确保：
- JDK 版本 >= 21
- 端口 8080 未被占用

### 3. 游戏无法启动

检查：
- 游戏路径设置正确
- 游戏可执行文件存在
- 有相应的文件权限

## 开发指南

### 添加新 API

1. 在 `controller` 包下创建或修改 Controller
2. 添加 `@RestController` 和 `@RequestMapping` 注解
3. 实现 API 方法并添加跨域支持 `@CrossOrigin(origins = "*")`

### 添加新页面

1. 在 `thmanager-frontend/src/views` 创建 Vue 组件
2. 在 `router/index.js` 添加路由
3. 在 `App.vue` 的菜单中添加导航项

### 添加新组件

1. 在 `thmanager-frontend/src/components` 创建组件
2. 在页面中导入使用

## 许可证

MIT License

## 致谢

- 东方Project - 上海爱丽丝幻乐团
- threp - Replay 解析库
