package munt;

class PassEnc {
    private byte[] f1;
    private int f2;
    private byte[] f3;
    
    public PassEnc(byte[] f1, int f2, byte[] f3) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
    }
    void setF1(byte[] f1) {
        this.f1 = f1;
    }

    byte[] getF1() {
        return f1;
    }

    void setF2(int f2) {
        this.f2 = f2;
    }

    int getF2() {
        return f2;
    }

    void setF3(byte[] f3) {
        this.f3 = f3;
    }

    byte[] getF3() {
        return f3;
    }
}
