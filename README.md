# 东方Project游戏管理器 (Touhou Manager)

一个基于 Vue 3 + Spring Boot 的东方Project游戏管理工具，支持游戏启动、Replay 管理、统计功能和路径配置。

## 项目结构

```
TouHouManager/
├── parser/
│   └── threp/            # Replay 解析库
├── src/
│   └── main/
│       ├── java/         # Spring Boot 后端
│       │   └── com/thmanager/
│       │       ├── controller/  # API 控制器
│       │       ├── dao/         # 数据访问
│       │       ├── mapper/      # MyBatis 映射
│       │       ├── model/       # 实体类
│       │       └── service/     # 业务逻辑
│       └── resources/    # 后端资源
│           ├── database/         # 数据库脚本
│           ├── static/           # 静态资源
│           └── application.properties  # 应用配置
├── thmanager-frontend/   # Vue 3 前端
│   ├── image/            # 游戏图片资源
│   │   ├── avatar/       # 游戏头像
│   │   └── main/         # 游戏主图
│   ├── music/            # 游戏音乐资源
│   ├── src/
│   │   ├── api/          # API 接口
│   │   ├── router/       # 路由配置
│   │   ├── stores/       # Pinia 状态管理
│   │   ├── views/        # 页面组件
│   │   ├── App.vue       # 根组件
│   │   └── main.js       # 入口文件
│   ├── index.html        # HTML 模板
│   ├── package.json      # 前端依赖
│   └── vite.config.js    # Vite 配置
├── .gitignore            # Git 忽略文件
├── README.md             # 项目说明
└── pom.xml               # Maven 配置
```

## 技术栈

### 后端
- **Spring Boot 3.2.0** - 主框架
- **Spring Web** - REST API
- **MyBatis Plus** - ORM 框架
- **SQLite** - 数据库
- **Maven** - 构建工具

### 前端
- **Vue 3** - 前端框架
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Axios** - HTTP 客户端
- **Vite** - 构建工具

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

前端开发服务器将在 http://localhost:3002 启动（端口可能会自动调整）

#### 3. 访问应用

打开浏览器访问 http://localhost:3002

### 方法二：生产模式

#### 1. 构建完整项目

```bash
# 在项目根目录执行
mvn clean package
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
- `PUT /api/games/{id}/path` - 更新游戏安装路径
- `POST /api/games/{id}/launch` - 启动游戏
- `POST /api/games/force-stop` - 强制停止游戏
- `GET /api/games/status` - 获取游戏运行状态
- `POST /api/games/clear-paths` - 清空所有游戏路径

### Replay 管理
- `GET /api/replays` - 获取所有 Replay
- `GET /api/replays/game/{gameId}` - 获取指定游戏的 Replay
- `GET /api/replays/{id}` - 获取单个 Replay
- `DELETE /api/replays/{id}` - 删除 Replay
- `POST /api/replays/scan` - 扫描新 Replay

## 功能特性

### 已实现
- ✅ 游戏库管理 - 查看所有东方Project游戏
- ✅ 游戏启动 - 支持倒计时启动
- ✅ 路径设置 - 批量设置游戏安装路径
- ✅ 安装状态检测 - 自动检测游戏是否已安装
- ✅ 路径清空 - 一键清空所有游戏路径
- ✅ Replay 管理 - 自动扫描、查看、删除 Replay
- ✅ 数据统计 - 游戏时长、分数等统计

### 待实现
- 📝 Replay 分析详情
- 📝 更多统计图表
- 📝 成就系统
- 📝 数据备份/恢复

## 配置说明

### 后端配置 (application.properties)

```properties
# 服务器端口
server.port=8080

# 数据库配置
spring.datasource.url=jdbc:sqlite:${user.home}/.touhou-manager/touhou_manager.db

# 其他配置...
```

### 前端配置 (vite.config.js)

```javascript
server: {
  port: 3002,
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

### 4. 路径显示异常

如果路径显示为 `{"path":"{\"path\":\"\"}"}`，请使用 "清空所有路径" 功能重置路径，然后重新设置。

## 开发指南

### 添加新 API

1. 在 `controller` 包下创建或修改 Controller
2. 添加 `@RestController` 和 `@RequestMapping` 注解
3. 实现 API 方法

### 添加新页面

1. 在 `thmanager-frontend/src/views` 创建 Vue 组件
2. 在 `router/index.js` 添加路由
3. 在相应的菜单中添加导航项

### 添加新组件

1. 在 `thmanager-frontend/src/components` 创建组件
2. 在页面中导入使用

## 许可证

MIT License

## 致谢

- 东方Project - 上海爱丽丝幻乐团
- threp - Replay 解析库

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，欢迎联系项目维护者。