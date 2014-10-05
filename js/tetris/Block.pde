public static class Block {

    public static final int SINGLE = 0, SQUARE = 1, REGULAR_L = 2, BACKWARDS_L = 3,
    ZIGZAG_HIGH_LEFT = 4, ZIGZAG_HIGH_RIGHT = 5, LINE = 6, T = 7;

    public static int getRandomBlock() {
        int nextBlockType = (int)(Math.floor(Math.random()*7)+1);

        switch(nextBlockType) {
            case 0: return SINGLE;
            case 1: return SQUARE;
            case 2: return REGULAR_L;
            case 3: return BACKWARDS_L;
            case 4: return ZIGZAG_HIGH_LEFT;
            case 5: return ZIGZAG_HIGH_RIGHT;
            case 6: return LINE;
            case 7: return T;
            default: return SINGLE;
        }
    }
}
