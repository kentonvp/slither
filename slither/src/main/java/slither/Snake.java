package slither;

import org.slf4j.*;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

public class Snake {
    private static final Logger logger = LoggerFactory.getLogger(Snake.class);

    private static final int MOVEMENT_DELTA = 1;

    private Point head;
    private Point addLocation;
    private List<Point> body = new ArrayList<>();
    private Point applePos;
    private Direction faceDirection = Direction.RIGHT;

    private boolean won = false;

    private int tickCount = 0;

    private int maxX;
    private int maxY;

    private Random rand;
    private long seed;

    private boolean ateOnLastStep = false;

    public Snake(int x, int y, int maxX, int maxY) {
        seed = new Random().nextInt(Integer.MAX_VALUE);
        rand = new Random(seed);

        this.maxX = maxX;
        this.maxY = maxY;
        head = new Point(x, y);
        logger.debug("Snake with start position {}", head);

        addLocation = head.copy();
        addLocation.decrementX(MOVEMENT_DELTA);

        applePos = randomApple();

        positionsLog();
    }

    public Snake(int x, int y, int maxX, int maxY, long seed) {
        this.seed = seed;
        rand = new Random(seed);

        this.maxX = maxX;
        this.maxY = maxY;
        head = new Point(x, y);
        logger.debug("Snake with start position {}", head);

        addLocation = head.copy();
        addLocation.decrementX(MOVEMENT_DELTA);

        applePos = randomApple();

        positionsLog();
    }

    public Snake(Snake that) {
        this.seed = that.seed;
        rand = new Random(seed);

        this.maxX = that.maxX;
        this.maxY = that.maxY;
        this.head = that.head.copy();

        this.addLocation = that.addLocation.copy();
        this.applePos = that.applePos.copy();
        this.faceDirection = that.faceDirection;

        for (int i = 0; i < that.body.size(); i++) {
            this.body.add(that.body.get(i).copy());
        }
    }

    public void positionsLog() {
        logger.debug("Head: {}", head);
        logger.debug("Body: {}", body);
        logger.debug("Apple: {}", applePos);
    }

    public boolean isWinner() {
        return won;
    }

    public void eat() {
        body.add(addLocation);
        applePos = randomApple();
    }

    public boolean ateOnLastStep() {
        return ateOnLastStep;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }

    private void followHead() {
        if (!body.isEmpty()) {
            addLocation = body.get(body.size() - 1).copy();
            for (int i = body.size() -1; i > 0; i--) {
                body.set(i, body.get(i-1).copy());
            }
            body.set(0, head.copy());
        } else {
            addLocation = head.copy();
        }
    }

    private void right() {
        followHead();
        head.incrementX(MOVEMENT_DELTA);
    }

    private void left() {
        followHead();
        head.decrementX(MOVEMENT_DELTA);
    }

    private void up() {
        followHead();
        head.decrementY(MOVEMENT_DELTA);
    }

    private void down() {
        followHead();
        head.incrementY(MOVEMENT_DELTA);
    }

    public Point getHeadPosition() {
        return head;
    }

    public int score() {
        return body.size();
    }

    public List<Point> getPositions() {
        // return the positions that are taken for
        ArrayList<Point> positions = new ArrayList<>();
        positions.add(head);
        if (!body.isEmpty()) {
            positions.addAll(body);
        }

        return positions;
    }

    public List<Point> getBody() {
        return body;
    }

    public Direction currentDirection() {
        return faceDirection;
    }

    public void setDirection(Direction direction) {
        if (direction != Direction.oppositeDirection(faceDirection)) {
            faceDirection = direction;
        }
    }

    public static Point nextPoint(Point x, Direction direction) {
        var next = x.copy();
        switch (direction) {
            case UP:
                next.decrementY(MOVEMENT_DELTA);
                break;
            case DOWN:
                next.incrementY(MOVEMENT_DELTA);
                break;
            case LEFT:
                next.decrementX(MOVEMENT_DELTA);
                break;
            case RIGHT:
                next.incrementX(MOVEMENT_DELTA);
                break;
            default:
                logger.error("Unknown Direction: {}", direction);
        }

        return next;
    }

    public synchronized void move() {
        switch (faceDirection) {
            case UP:
                up();
                break;
            case DOWN:
                down();
                break;
            case LEFT:
                left();
                break;
            case RIGHT:
                right();
                break;
            default:
                logger.error("Unknown Direction: {}", faceDirection);
        }

        positionsLog();

        ateOnLastStep = applePos.equals(head);
        if (ateOnLastStep) {
            eat();
        }

        tickCount++;
    }

    public int getTotalTicks() {
        return tickCount;
    }

    public Point applePosition() {
        return applePos;
    }

    private Point randomApple() {
        var availablePositions = getAvailablePositions();
        if (availablePositions.isEmpty()) {
            won = true;
            return null;
        }

        int index = rand.nextInt(availablePositions.size());
        var iter = availablePositions.iterator();
        Point nextApple = null;
        for (int i = 0; i <= index; i++) {
            nextApple = iter.next();
        }

        return nextApple;
    }

    private Set<Point> allPositions() {
        var valid = new HashSet<Point>();
        for (int i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                valid.add(new Point(i, j));
            }
        }
        return valid;
    }

    public Set<Point> getAvailablePositions() {
        var valid = allPositions();
        List<Point> snake = getPositions();
        for (Point p: snake)
            valid.remove(p);
        return valid;
    }

    public synchronized boolean didCollide() {
        return body.stream().anyMatch(b -> b.equals(head));
    }

    public synchronized boolean outOfBounds() {
        return head.getX() < 0 || head.getX() >= maxX || head.getY() < 0 || head.getY() >= maxY;
    }

    public long randSeed() {
        return seed;
    }
}
