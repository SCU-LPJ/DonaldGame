# 项目结构
```bash
DonaldGame/
├─ src/
│  ├─ model/           # 数据与角色建模（唐老鸭、唐小鸭）
│  ├─ view/            # 图形界面（Swing UI层）
│  ├─ controller/      # 控制层（处理用户输入、事件监听、场景切换）
│  ├─ service/         # 业务逻辑层（后续可添加AI对话、发红包、统计代码等）
│  ├─ util/            # 工具类（关键字匹配、资源加载）
│  └─ Main.java        # 程序入口
├─ resources/          # 图片、音效、配置文件
│  ├─ images/
│  │  ├─ donald.png
│  │  ├─ duck_red.png
│  │  ├─ duck_blue.png
│  │  └─ duck_yellow.png
│  └─ config/
│     └─ keywords.json  # 关键词配置
└─ README.md

```

```bash
com.duckgame
├── main
│   └── Main.java                  // 程序入口
│
├── ui
│   ├── GameFrame.java             // 主窗口（包含鸭子和对话区）
│   ├── DuckPanel.java             // 绘制鸭子组件面板
│   ├── DialogPanel.java           // 用户对话输入面板
│
├── model
│   ├── Duck.java                  // 鸭子抽象类（公有属性：名字、颜色、位置等）
│   ├── DonaldDuck.java            // 唐老鸭（可交互）
│   ├── LittleDuck.java            // 唐小鸭（有不同喜好）
│   ├── HobbyType.java             // 枚举类：喜好类型（发红包、玩游戏、睡觉）
│
├── controller
│   ├── GameController.java        // 控制 UI 与逻辑交互
│   └── KeywordManager.java        // 管理关键词与功能映射（后续可扩展）
│
└── util
    └── ImageLoader.java           // 工具类：加载图片资源

```

```plaintext
                ┌───────────────────────────┐
                │        GameController     │
                │───────────────────────────│
                │ - ducks: List<Duck>       │
                │ - dialogPanel: DialogPanel│
                │ - duckPanel: DuckPanel    │
                │───────────────────────────│
                │ + handleUserInput(String) │
                │ + updateUI()              │
                └───────────▲───────────────┘
                            │
                            │ uses
                            │
┌────────────────────────────────────┐        ┌─────────────────────┐
│             GameFrame              │<>─────▶│    GameController   │
│────────────────────────────────────│        └─────────────────────┘
│ - duckPanel: DuckPanel             │
│ - dialogPanel: DialogPanel         │
│────────────────────────────────────│
│ + initUI()                         │
│ + main(String[])                   │
└────────────────────────────────────┘
           ▲                 ▲
           │ contains        │ contains
           │                 │
 ┌────────────────────┐     ┌────────────────────┐
 │     DuckPanel      │     │   DialogPanel      │
 │────────────────────│     │────────────────────│
 │ + paintComponent() │     │ + getUserInput()   │
 │ + addClickListener()│    │ + showResponse()   │
 └────────────────────┘     └────────────────────┘


                   ┌───────────────────────────────┐
                   │            Duck               │
                   │───────────────────────────────│
                   │ - name: String                │
                   │ - color: Color                │
                   │ - position: Point             │
                   │───────────────────────────────│
                   │ + draw(Graphics g)            │
                   │ + onClick()                   │
                   └──────────▲───────────┬────────┘
                              │           │
         ┌────────────────────┘           └────────────────────┐
         │                                               │
┌────────────────────────────┐              ┌──────────────────────────┐
│       DonaldDuck           │              │        LittleDuck        │
│────────────────────────────│              │──────────────────────────│
│ + onClick(): void           │              │ - hobby: HobbyType       │
│ + speak(): String           │              │ + performHobby(): void   │
└────────────────────────────┘              └──────────────────────────┘


┌──────────────────────────┐
│       HobbyType          │
│──────────────────────────│
│ + RED_PACKET             │
│ + GAME                   │
│ + SLEEP                  │
└──────────────────────────┘

```