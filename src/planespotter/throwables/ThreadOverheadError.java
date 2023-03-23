package planespotter.throwables;

public class ThreadOverheadError extends Error {

    public ThreadOverheadError () {
        super("\n ERROR! thread-overhead! maximum poolsize reached!");
    }
}
