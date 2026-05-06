#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <windows.h>

typedef struct {
    int32_t x;
    int32_t y;
} Point;

static volatile int64_t blackhole;

static int64_t nanos_now(void) {
    static LARGE_INTEGER frequency;
    static int initialized;
    LARGE_INTEGER counter;

    if (!initialized) {
        QueryPerformanceFrequency(&frequency);
        initialized = 1;
    }

    QueryPerformanceCounter(&counter);
    return (int64_t)((counter.QuadPart * 1000000000LL) / frequency.QuadPart);
}

static void print_result(const char *label, int64_t nanos) {
    printf("%-28s %8lld ms\n", label, (long long)(nanos / 1000000LL));
}

static void fill_points(Point *points, int size) {
    for (int i = 0; i < size; i++) {
        points[i].x = i;
        points[i].y = i + 1;
    }
}

static void native_write(int size, int loops) {
    Point *points = malloc((size_t)size * sizeof(Point));
    if (points == NULL) {
        fprintf(stderr, "malloc failed\n");
        exit(1);
    }

    int64_t start = nanos_now();
    for (int r = 0; r < loops; r++) {
        for (int i = 0; i < size; i++) {
            points[i].x = i;
            points[i].y = i + 1;
        }
    }
    int64_t end = nanos_now();

    blackhole ^= points[size - 1].x;
    print_result("Native struct write:", end - start);
    free(points);
}

static void native_read(int size, int loops) {
    Point *points = malloc((size_t)size * sizeof(Point));
    if (points == NULL) {
        fprintf(stderr, "malloc failed\n");
        exit(1);
    }

    fill_points(points, size);

    int64_t start = nanos_now();
    int64_t sum = 0;
    for (int r = 0; r < loops; r++) {
        for (int i = 0; i < size; i++) {
            sum += points[i].x;
            sum += points[i].y;
        }
    }
    int64_t end = nanos_now();

    blackhole ^= sum;
    print_result("Native struct read sum:", end - start);
    free(points);
}

static void native_set_function(Point *points, int index, Point point) {
    points[index] = point;
}

static Point native_get_function(Point *points, int index) {
    return points[index];
}

static void native_function_write(int size, int loops) {
    Point *points = malloc((size_t)size * sizeof(Point));
    if (points == NULL) {
        fprintf(stderr, "malloc failed\n");
        exit(1);
    }

    int64_t start = nanos_now();
    for (int r = 0; r < loops; r++) {
        for (int i = 0; i < size; i++) {
            native_set_function(points, i, (Point){i, i + 1});
        }
    }
    int64_t end = nanos_now();

    blackhole ^= points[size - 1].y;
    print_result("Native function write:", end - start);
    free(points);
}

static void native_function_read(int size, int loops) {
    Point *points = malloc((size_t)size * sizeof(Point));
    if (points == NULL) {
        fprintf(stderr, "malloc failed\n");
        exit(1);
    }

    fill_points(points, size);

    int64_t start = nanos_now();
    int64_t sum = 0;
    for (int r = 0; r < loops; r++) {
        for (int i = 0; i < size; i++) {
            Point point = native_get_function(points, i);
            sum += point.x;
            sum += point.y;
        }
    }
    int64_t end = nanos_now();

    blackhole ^= sum;
    print_result("Native function read:", end - start);
    free(points);
}

int main(int argc, char **argv) {
    int size = argc > 1 ? atoi(argv[1]) : 5000000;
    int loops = argc > 2 ? atoi(argv[2]) : 3;

    if (size <= 0 || loops <= 0) {
        fprintf(stderr, "Usage: MemNativeBenchmark [positive-size] [positive-loops]\n");
        return 1;
    }

    printf("MemNativeBenchmark\n");
    printf("size=%d, loops=%d\n\n", size, loops);

    native_write(size, loops);
    native_function_write(size, loops);
    native_read(size, loops);
    native_function_read(size, loops);

    printf("\nblackhole=%lld\n", (long long)blackhole);
    return 0;
}
