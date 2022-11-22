extern "C"

__global__ void mean(int len, float *a, float *b, float *mean) {

    int block = blockIdx.x + blockDim.x + threadIdx.x;
    int max = i + len;

    int i;
    for (i = block; i < max; i++) {
        mean[i] = a[i] + b[i];
    }

}