

// 0-上，1-下(默认)，2-左，3-右
var up = 0, down = 1, left = 2, right = 3;

/**
 *
 * @type {{}}
 */
class Snake {

    squareItemList = [];

    constructor(snakeHead) {
        this.greedySnakeCanvas = greedySnakeCanvas;
        this.snakeHead = snakeHead;
        this.squareItemList.push(snakeHead);
    }

    drawSnake() {
        for (var item of this.squareItemList) {
            item.draw(this.greedySnakeCanvas.ctx);
        }
    }

    /**
     * 从尾部开始改变方向，当前方块方向要和前面方块一样
     */
    move(moveDirection) {

        // 蛇头方向更新
        this.snakeHead.moveDirection = moveDirection;

        this.eatFood(this.greedySnakeCanvas.food);

        var isMove = this.snakeHead.move(this.greedySnakeCanvas.unit, this.greedySnakeCanvas.canvasWidth
            , this.greedySnakeCanvas.canvasHeight);
        if (isMove) {
            for (let i = 1; i < this.squareItemList.length; i++) {
                var curItem = this.squareItemList[i];
                curItem.move(this.greedySnakeCanvas.unit, this.greedySnakeCanvas.canvasWidth
                    , this.greedySnakeCanvas.canvasHeight);
            }

            this.greedySnakeCanvas.draw();

            for (let i = this.squareItemList.length - 1; i > 0; i--) {
                var prevItem = this.squareItemList[i - 1];
                var curItem = this.squareItemList[i];
                curItem.moveDirection = prevItem.moveDirection;
            }
        }
    }

    /**
     * 吃食物 需要判断两个方块是否相交，两个方块的横轴和纵轴距离是否小于一个单元格
     * @param food
     */
    eatFood(food) {
        var isEat = false;

        // 判断是否吃到了食物，需要在一定范围内，因为是根据左上角定位，用这个处理，考虑正负情况
        var xDiff = food.x - this.snakeHead.x;
        var yDiff = food.y - this.snakeHead.y;
        if (xDiff < this.snakeHead.len + food.len && xDiff > -this.snakeHead.len - food.len) {
            if (yDiff < this.snakeHead.len + food.len && yDiff > -this.snakeHead.len - food.len) {
                isEat = true;
            }
        }

        // 在蛇头前面创建方块
        if (isEat) {
            var x, y;
            switch (this.snakeHead.moveDirection) {
                case up:
                    x = this.snakeHead.x;
                    y = this.snakeHead.y - food.len;
                    break;
                case down:
                    x = this.snakeHead.x;
                    y = this.snakeHead.y + food.len;
                    break;
                case left:
                    x = this.snakeHead.x - food.len;
                    y = this.snakeHead.y;
                    break;
                case right:
                    x = this.snakeHead.x + food.len;
                    y = this.snakeHead.y;
                    break;
            }
            // 和蛇头方向一致
            var newSnakeHead = new SquareItem(x, y, food.len, this.snakeHead.moveDirection);
            // 将蛇头延长蛇头比较方便
            this.squareItemList.unshift(newSnakeHead);
            this.snakeHead = newSnakeHead;
            // 食物被吃了，需要重写随机一个
            this.greedySnakeCanvas.randomFood();
        }
    }
}

class SquareItem {
    constructor(x, y, len, moveDirection) {
        this.x = x;
        this.y = y;
        this.len = len;
        this.moveDirection = moveDirection;
    }

    draw(ctx) {
        ctx.fillRect(this.x, this.y, this.len, this.len);
    }

    move(distance, canvasWidth, canvasHeight) {
        switch (this.moveDirection) {
            case up:
                if (this.y - distance > 0) {
                    this.y -= distance;
                    return true;
                }
            case down:
                // 考虑边界条件，2倍的距离，因为是x，y是左上角，所以要算上方块长度和距离
                if (this.y + this.len + distance <= canvasHeight) {
                    this.y += distance;
                    return true;
                }
            case left:
                if (this.x - distance > 0) {
                    this.x -= distance;
                    return true;
                }
            case right:
                // 考虑边界条件，2倍的距离，因为是x，y是左上角，所以要算上方块长度和距离
                if (this.x + this.len + distance <= canvasWidth) {
                    this.x += distance;
                    return true;
                }
        }
        return false;
    }
}

class GreedySnakeCanvas{
    // 画布的长宽
    canvasWidth = 0;
    canvasHeight = 0;
    unit = 20;

    // 蛇的数量
    snakeList = [];

    food = null;

    constructor(ctx) {
        this.ctx = ctx;

        // 窗口调整自动调用画布大小调整
        $(window).resize(this.resizeCanvas);
        this.resizeCanvas();

        this.randomFood();
    }

    initNewSnakeHead() {
        // 方块的左上角位置
        // 出现在正中间
        var x = this.canvasWidth / 2 - this.unit / 2;
        var y = this.canvasHeight / 2 - this.unit / 2;
        var snake = new Snake(new SquareItem(x, y, this.unit, down));
        this.snakeList.push(snake);

        this.registerKeyListener(snake);
    }

    draw() {
        ctx.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
        if (this.food) {
            this.food.draw(this.ctx);
        }

        for (var snake of this.snakeList) {
            snake.drawSnake();
        }
    }

    randomFood() {
        var x = Math.random() * this.canvasWidth;
        var y = Math.random() * this.canvasHeight;
        if (this.food) {
            this.ctx.clearRect(this.food.x, this.food.y, this.food.len, this.food.len);
        }
        x + this.unit > this.canvasWidth ? x = x - this.unit : x;
        y + this.unit > this.canvasHeight ? y = y - this.unit : y;
        this.food = new SquareItem(x, y, this.unit);
        this.food.draw(this.ctx);
    }

    resizeCanvas() {
        var windowWidth = $(window).get(0).innerWidth;
        var windowHeight = $(window).get(0).innerHeight;
        this.canvasWidth = windowWidth - windowWidth * 0.2;
        this.canvasHeight = windowHeight - windowHeight * 0.2;
        $('#myCanvas').attr("width", this.canvasWidth);
        $('#myCanvas').attr("height", this.canvasHeight);
    }

    registerKeyListener(snake) {
        // 键盘事件
        document.body.onkeydown = function (e) {
            switch (e.key) {
                case 'ArrowUp':
                    snake.move(up);
                    break;
                case 'ArrowDown':
                    snake.move(down);
                    break;
                case 'ArrowLeft':
                    snake.move(left);
                    break;
                case 'ArrowRight':
                    snake.move(right);
                    break;

            }
        };
    }
}

//颜色-黑色，背景-白色
// 操作画布的对象
// var ctx = $('#myCanvas').get(0).getContext("2d");
// var greedySnakeCanvas = new GreedySnakeCanvas(ctx);

// 设置移动定时器
// function setMoveInterval() {doMove
//     var moveInterval = setInterval(function () {
//         snake.move(snake.snakeHead.moveDirection);
//     }, 500);
// };

// setMoveInterval();