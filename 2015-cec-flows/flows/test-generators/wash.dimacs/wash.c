  /* makegraph.c */

/* graph.h */

#include <sys/time.h>
#include <stdio.h>
/*#include <time.h>
*/
#include <ctype.h>
#include <strings.h>

/* ghead.h */

#define DATA_DIRECTORY "/local/users/anderson/netflow/data/"

#define FAILURE    0
#define SUCCESS    1
#define FALSE      0
#define TRUE       1


#define MAX_N     10000000  
/* #define MAX_N 60 */

#define MAX_CAP   100000000

/* Dimacs problem types */
#define UNDEFINED        0
#define MINCOSTFLOW      1
#define MAXFLOW          2
#define ASSIGNMENT       3

typedef struct enode {
  struct enode *next;
  struct enode *mate;
  int c;
  int f;
  int h;
  int t;
  int flag;
} Edge;


typedef struct {
  Edge *A[MAX_N];
  int V[MAX_N];
  int size;
  int max_v;
} Graph;

typedef struct {
  int head, tail, size;
  int *data;
} Queue;





#define MAX_RANGE 1000000
#define MAX_DEGREE 20
#define VERY_BIG 1000000

int Range[] = {1000000, 500000, 250000, 125000, 62500, 31250,
		 15625, 7812, 3906, 1953, 976, 488, 244, 122,
		 61, 31, 15, 7, 4, 2};


main(argc, argv)
int argc;
char *argv[];
{
  Graph *G, *Mesh(), *RLevel(), *R2Level(), *Match(), *SquareMesh(), 
        *BasicLine(), *ExponentialLine(), *DExponentialLine(), *DinicBadCase(),
        *GoldBadCase(), *Cheryian();

  FILE *f;
  int dim1, dim2, range, fct, s, t, seed;


  if (argc < 3)
    Barf("Usage: wash <seed> <fct> <arg1> <arg2> <arg3>\n\n\
\n\
  Command line arguments have the following meanings: \n\
\n\
  fct:               index of desired graph type \n\
  arg1, arg2, arg3:  meanings depend on graph type \n\
                     (briefly listed below: see code comments for more info)\n\
\n\
  fct  Graphtype                Parameter\n\
    1  Mesh Graph               rows cols maxcapacity \n\
    2  Random Level Graph       rows cols maxcapacity \n\
    3  Random 2-Level Graph     rows cols maxcapacity \n\
    4  Matching Graph           vertices degree \n\
    5  Square Mesh              side degree maxcapacity\n\
    6  Basic Line               rows cols degree \n\
    7  Exponential Line         rows cols degree \n\
    8  Double Exponential Line  rows cols degree\n\
    9  DinicBadCase             vertices (causes n augmentation phases) \n\
   10  GoldBadCase              vertices \n\
   11  Cheryian                 dim1 dim2 range\n\n");

  seed = atoi(argv[1]);
  InitRandom(seed);

  fct = atoi(argv[2]);
  dim1 = atoi(argv[3]);

  if (fct != 9 && fct != 10)
  {
    dim2 = atoi(argv[4]);
    if (fct != 4) range = atoi(argv[5]);
  }
  
  switch(fct){
  case 1:
    printf("c Mesh Graph\n");
    printf("c %d Rows, %d columns, capacities in range [0, %d] (seed: %d)\n",
	    dim1, dim2, range,seed);
    G = Mesh(dim1, dim2, range);
    s = 0;
    t = G->size - 1;
    break;
  case 2:
    printf("c Random Leveled Graph\n");
    printf("c %d Rows, %d columns, capacities in range [0, %d] (seed: %d)\n",
	    dim1, dim2, range,seed);
    G = RLevel(dim1, dim2, range);
    s = 0;
    t = G->size - 1;
    break;
  case 3:
    printf("c Random 2 Leveled Graph\n");
    printf("c %d Rows, %d columns, capacities in range [0, %d] (seed: %d)\n",
	    dim1, dim2, range);
    G = RLevel(dim1, dim2, range);
    s = 0;
    t = G->size - 1;
    break;
  case 4:
    printf("c Matching Graph\n");
    printf("c %d vertices, %d degree (seed: %d)\n", dim1, dim2,seed);
    G = Match(dim1, dim2);
    s = 0;
    t = G->size - 1;
    break;

  case 5:
    printf("c Square Mesh\n");
    printf("c %d x %d vertices, %d degree, range [0,%d] (seed: %d)\n", 
	    dim1, dim1, dim2, range,seed);
    G = SquareMesh(dim1, dim2, range);
    s = 0;
    t = G->size - 1;
    break;

  case 6:
    printf("c Basic Line Mesh\n");
    printf("c %d x %d vertices, degree d (seed: %d)\n", 
	    dim1, dim2, range,seed);
    G = BasicLine(dim1, dim2, range);
    s = 0;
    t = G->size - 1;
    break;

  case 7:
    printf("c Exponential Line\n");
    printf("c %d x %d vertices, degree %d (seed: %d)\n", 
	    dim1, dim2, range,seed);
    G = ExponentialLine(dim1, dim2, range);
    s = 0;
    t = G->size - 1;
    break;

  case 8:
    printf("c Double Exponential Line\n");
    printf("c %d x %d vertices, degree %d (seed: %d)\n", 
	    dim1, dim2, range,seed);
    G = DExponentialLine(dim1, dim2, range);
    s = 0;
    t = G->size - 1;
    break;

  case 9:
    printf("c Line Graph - Bad case for Dinics (seed: %d)\n",seed);
    printf("c %d vertices\n", dim1);
    G = DinicBadCase(dim1);
    s = 0;
    t = G->size - 1;
    break;

  case 10:
    printf("c  Bad case for Goldberg (seed: %d)\n",seed);
    printf("c %d vertices\n", dim1);
    G = GoldBadCase(dim1);
    s = 0;
    t = G->size - 1;
    break;

  case 11:
    printf("c  Cheryian Graph\n");
    printf("c n = %d, m = %d, c = %d, total vertices %d  (seed: %d)\n", 
	    dim1, dim2, range, 4*dim2*range + 6 + dim1,seed);
    G = Cheryian(dim1, dim2, range);    
    s = 0;
    t = G->size - 1;
    break;

  default:
    Barf("Undefined class");
    break;

  }

  GraphOutput(G, f, s, t);
}

Graph *Mesh(d1, d2, r)
int d1, d2, r;
{
  Graph *G;
  int i, j, source, sink;

  if (d1 < 2 || d2 < 2)
    Barf("Degenerate graph");
  if (d1*d2 + 2 > MAX_N)
    Barf("Graph out of range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i <= d1*d2 + 1; i++)
    AddVertex(i, G);

  source = 0;
  sink = d1*d2 + 1;

  for (i = 1; i <= d1; i++){
    AddEdge(source, source + i, 3*r, G);
    AddEdge(sink - i, sink, 3*r, G);
  }

  for (i = 0; i < d2 - 1; i++){
    AddEdge(i*d1 + 1, (i+1)*d1 + d1, RandomInteger(1, r), G);
    AddEdge(i*d1 + 1, (i+1)*d1 + 1, RandomInteger(1, r), G);
    AddEdge(i*d1 + 1, (i+1)*d1 + 2, RandomInteger(1, r), G);
    for (j = 2; j <= d1 - 1; j++){
      AddEdge(i*d1 + j, (i+1)*d1 + j - 1, RandomInteger(1, r), G);
      AddEdge(i*d1 + j, (i+1)*d1 + j, RandomInteger(1, r), G);
      AddEdge(i*d1 + j, (i+1)*d1 + j + 1, RandomInteger(1, r), G);
    }
    AddEdge(i*d1 + d1, (i+1)*d1 + d1 - 1, RandomInteger(1, r), G);
    AddEdge(i*d1 + d1, (i+1)*d1 + d1, RandomInteger(1, r), G);
    AddEdge(i*d1 + d1, (i+1)*d1 + 1, RandomInteger(1, r), G);
  }

  return G;
}


Graph *RLevel(d1, d2, r)
int d1, d2, r;
{
  Graph *G;
  int i, j, source, sink, x[3];

  if (d1 < 2 || d2 < 2)
    Barf("Degenerate graph");
  if (d1*d2 + 2 > MAX_N)
    Barf("Graph out of range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i <= d1*d2 + 1; i++)
    AddVertex(i, G);

  source = 0;
  sink = d1*d2 + 1;

  for (i = 1; i <= d1; i++){
    AddEdge(source, source + i, 3*r, G);
    AddEdge(sink - i, sink, 3*r, G);
  }

  for (i = 0; i < d2 - 1; i++){
    for (j = 1; j <= d1; j++){    
      RandomSubset(1, d1, 3, x);
      AddEdge(i*d1 + j, (i+1)*d1 + x[0], RandomInteger(1, r), G);
      AddEdge(i*d1 + j, (i+1)*d1 + x[1], RandomInteger(1, r), G);
      AddEdge(i*d1 + j, (i+1)*d1 + x[2], RandomInteger(1, r), G);
    }
  }

  return G;
}


Graph *R2Level(d1, d2, r)
int d1, d2, r;
{
  Graph *G;
  int i, j, source, sink, x[3];

  if (d1 < 2 || d2 < 2)
    Barf("Degenerate graph");
  if (d1*d2 + 2 > MAX_N)
    Barf("Graph out of range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i <= d1*d2 + 1; i++)
    AddVertex(i, G);

  source = 0;
  sink = d1*d2 + 1;

  for (i = 1; i <= d1; i++){
    AddEdge(source, source + i, 3*r, G);
    AddEdge(sink - i, sink, 3*r, G);
  }

  for (i = 0; i < d2 - 2; i++){
    for (j = 1; j <= d1; j++){    
      RandomSubset(1, 2*d1, 3, x);
      AddEdge(i*d1 + j, (i+1)*d1 + x[0], RandomInteger(1, r), G);
      AddEdge(i*d1 + j, (i+1)*d1 + x[1], RandomInteger(1, r), G);
      AddEdge(i*d1 + j, (i+1)*d1 + x[2], RandomInteger(1, r), G);
    }
  }
  for (j = 1; j <= d1; j++){    
    RandomSubset(1, d1, 3, x);
      AddEdge((d2-2)*d1 + j, (d1-1)*d1 + x[0], RandomInteger(1, r), G);
      AddEdge((d2-2)*d1 + j, (d1-1)*d1 + x[1], RandomInteger(1, r), G);
      AddEdge((d2-2)*d1 + j, (d1-1)*d1 + x[2], RandomInteger(1, r), G);
    }

  return G;
}




Graph *Match(n, d)
int n, d;
{
  Graph *G;
  int i, j, source, sink, x[2000000];

  if (n < 2 || d > n)
    Barf("Degenerate graph");
  if (2*n + 2 > MAX_N)
    Barf("Graph out of range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i <= 2*n + 1; i++)
    AddVertex(i, G);

  source = 0;
  sink = 2*n + 1;

  for (i = 1; i <= n; i++){
    AddEdge(source, source + i, 1, G);
    AddEdge(sink - i, sink, 1, G);
  }


  for (j = 1; j <= n; j++){    
    RandomSubset(1, n, d, x);
    for (i = 0; i < d; i++)
      AddEdge(j, n + x[i], 1, G);
  }
  return G;
}


Graph *SquareMesh(d, deg, r)
int d, deg, r;
{
  Graph *G;
  int i, j, k, source, sink;

  if (d < deg)
    Barf("Degenerate graph");
  if (d*d + 2 > MAX_N)
    Barf("Graph out of range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i <= d*d + 1; i++)
    AddVertex(i, G);

  source = 0;
  sink = d*d + 1;

  for (i = 1; i <= d; i++){
    AddEdge(source, source + i, 3*r, G);
    AddEdge(sink - i, sink, 3*r, G);
  }

  for (i = 0; i < d - 1; i++)
    for (j = 1; j <= d; j++)
      for (k = 0; k < deg; k++)
	if ((i+1)*d + j + k<= sink - 1)
	  AddEdge(i*d + j, (i+1)*d + j + k, RandomInteger(1, r), G);


  return G;
}


Graph *BasicLine(n, m, deg)
int n, m, deg;
{
  Graph *G;
  int i, j, source, sink, x[MAX_DEGREE];

  if (n*m > MAX_N)
    Barf("Graph out of range");
  if (deg > MAX_DEGREE)
    Barf("Degree Out of Range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i <= n*m + 1; i++)
    AddVertex(i, G);

  source = 0;
  sink = n*m + 1;

  for (i = 1; i <= m; i++){
    AddEdge(source, source + i, MAX_DEGREE*MAX_RANGE, G);
    AddEdge(sink - i, sink, MAX_DEGREE*MAX_RANGE, G);
  }

  for (i = source + 1; i < sink; i++){
      RandomSubset(1, m*deg, deg, x);
      for (j = 0; j < deg; j++)
	if (i + x[j] < sink)
	  AddEdge(i, i + x[j], RandomInteger(1, MAX_RANGE), G);
    }

  return G;
}




Graph *ExponentialLine(n, m, deg)
int n, m, deg;
{
  Graph *G;
  int i, j, source, sink, x[MAX_DEGREE], r;

  if (n*m > MAX_N)
    Barf("Graph out of range");
  if (deg > MAX_DEGREE)
    Barf("Degree Out of Range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i <= n*m + 1; i++)
    AddVertex(i, G);

  source = 0;
  sink = n*m + 1;

  for (i = 1; i <= m; i++){
    AddEdge(source, source + i, MAX_DEGREE*MAX_RANGE, G);
    AddEdge(sink - i, sink, MAX_DEGREE*MAX_RANGE, G);
  }

  for (i = source + 1; i < sink; i++){
      RandomSubset(1, m*deg, deg, x);
      for (j = 0; j < deg; j++){
	r = (x[j] - 1) / m;
	if (i + x[j] < sink)
	  AddEdge(i, i + x[j], RandomInteger(1, Range[r]), G);
      }
    }

  return G;
}

Graph *DExponentialLine(n, m, deg)
int n, m, deg;
{
  Graph *G;
  int i, j, source, sink, x[MAX_DEGREE], r;

  if (n*m > MAX_N)
    Barf("Graph out of range");
  if (deg > MAX_DEGREE)
    Barf("Degree Out of Range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i <= n*m + 1; i++)
    AddVertex(i, G);

  source = 0;
  sink = n*m + 1;

  for (i = 1; i <= m; i++){
    AddEdge(source, source + i, MAX_DEGREE*MAX_RANGE, G);
    AddEdge(sink - i, sink, MAX_DEGREE*MAX_RANGE, G);
  }

  for (i = source + 1; i < sink; i++){
      RandomSubset(-m*deg, m*deg, deg, x);
      for (j = 0; j < deg; j++){
	r = Abs((x[j] - 1) / m);
	if (i + x[j] < sink && i + x[j] > source && x[j] != 0)
	  AddEdge(i, i + x[j], RandomInteger(1, Range[r]), G);
      }
    }

  return G;
}


Graph *DinicBadCase(n)
int n;
{
  Graph *G;
  int i;

  if (n > MAX_N)
    Barf("Graph out of range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i < n; i++)
    AddVertex(i, G);

  for (i = 0; i < n-1; i++){
    AddEdge(i, i+1, n, G);
  }

  for (i = 0; i < n-2; i++){
    AddEdge(i, n - 1, 1, G);
  }


  return G;
}

Graph *GoldBadCase(n)
int n;
{
  Graph *G;
  int i;

  if (3*n+3 > MAX_N)
    Barf("Graph out of range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  for (i = 0; i < 3*n+3; i++)
    AddVertex(i, G);

  AddEdge(0, 1, n, G);

  for (i = 2; i < n+2; i++){
    AddEdge(1, i, n, G);
    AddEdge(i, i+n, 1, G);
    AddEdge(i+n, 2*n+2, n, G);
  }

  for (i = 2*n+2; i < 3*n+2; i++)
    AddEdge(i, i+1, n, G);

  return G;
}


Graph *Cheryian(n, m, c)
int n, m, c;
{
  Graph *G;
  int i;

  if (4*m*c + 6 + n > MAX_N)
    Barf("Graph out of range");

  G = (Graph *) malloc(sizeof(Graph));

  InitGraph(G);  

  AddVertex(0, G);
  AddVertex(1, G);
  AddVertex(2, G);
  AddVertex(3, G);

  Gadget(0, 1, n, m, c, G);
  Gadget(0, 2, n, m, c, G);
  Gadget(1, 3, n, m, c, G);
  Gadget(2, 3, n, m, c, G);

  Bridge(1, 2, n, G);

  Sink(3, G);

  return G;
}

Gadget(a, b, n, m, c, G)
int a, b, n, m, c;
Graph *G;
{
  int i, j, v, w;

  v = b;
  for (i = 0; i < m; i++){
    for (j = 0; j < c; j++){
      w = v;
      v = NewVertex(G);
      AddEdge(v, w, VERY_BIG, G);
    }
    AddEdge(a, v, n, G);
  }
}

Bridge(a, b, n, G)
int a, b, n;
Graph *G;
{
  int i, v, w,  v1, v2;

  v1 = NewVertex(G);
  v2 = NewVertex(G);
  AddEdge(a, v1, n, G);
  AddEdge(v2, b, n, G);
  for (i = 0; i < n; i++){
    v = NewVertex(G);
    w = NewVertex(G);
    AddEdge(v1, v, n, G);
    AddEdge(w, v2, n, G);
    AddEdge(v, w, 1, G);
  }
}

Sink(k, G)
int k;
Graph *G;
{
  AddEdge(k, NewVertex(G), VERY_BIG, G);
}

NewVertex(G)
Graph *G;
{
  AddVertex(G->size, G);
  return G->size - 1;
}


/* manip.c */


InitGraph(G)
Graph *G;
{
  int i;

  for (i = 0; i < MAX_N; i++){
    G->A[i] = (Edge *) 0;
    G->V[i] = FALSE;
  }
  G->size = 0;  
  G->max_v = -1;
}

Graph *CopyGraph(G1)
Graph *G1;
{
  int i;
  Edge *e;
  Graph *G2;

  G2 = (Graph *) malloc(sizeof(Graph));
  InitGraph(G2);

  for (i = 0; i <= G1->max_v; i++){
    if (G1->V[i] == TRUE){
      AddVertex(i, G2);
      e = G1->A[i];
      while (e != (Edge *) 0){
	if (e->c > 0)
	  AddEdge(i, e->h, e->c, G2);
	e = e->next;
      }
    }
  }

  return G2;
}

AddVertex(v, G)
int v;
Graph *G;
{
  if (G->V[v] == TRUE)
    Barf("Vertex already present");

  G->V[v] = TRUE;
  G->size++;
  if (v > G->max_v)
    G->max_v = v;
}


AddEdge(v1, v2, a, G)
int v1, v2, a;
Graph *G;
{
  Edge *e1, *e2, *EdgeLookup();

  if (v1 == v2)
    Barf("No Loops");

  if ((e1 = EdgeLookup(v1, v2, G)) != (Edge *) 0){
    e1->c += a;
    return;
  }

  e1 = (Edge *) malloc(sizeof(Edge));
  e2 = (Edge *) malloc(sizeof(Edge));

  e1->mate = e2;
  e2->mate = e1;

  e1->next = G->A[v1];
  G->A[v1] = e1;
  e1->t = v1;
  e1->h = v2;
  e1->c = a;

  e2->next = G->A[v2];
  G->A[v2] = e2;
  e2->t = v2;
  e2->h = v1;
  e2->c = 0;
}

Edge *EdgeLookup(v1, v2, G)
int v1, v2;
Graph *G;
{
  Edge *e;

  e = G->A[v1];
  while (e != (Edge *) 0){
    if (e->h == v2)
      return e;
    e = e->next;
  }
  return (Edge *) 0;
}

UEdgeArray(E, m, G)
Edge *E[];
int m;
Graph *G;
{
  int i, count;
  Edge *e;

  count = 0;
  for (i = 0; i <= G->max_v; i++){
    if (G->V[i] == FALSE)
      continue;
    e = G->A[i];
    while (e != (Edge *) 0){
      if (e->h < e->t){
	if (count == m)
	  Barf("UEdgeArray overflow");
        E[count] = e;
	count++;
      }
      e = e->next;
    }
  }
}

/* Count the number of edges with positive capacity */
int EdgeCount(G)
Graph *G;
{
  int i, count;
  Edge *e;

  count = 0;
  for (i = 0; i <= G->max_v; i++){
    if (G->V[i] == FALSE)
      continue;
    e = G->A[i];
    while (e != (Edge *) 0){
      if (e->c > 0)
	count++;
      e = e->next;
    }
  }
  return count;
}
  


/* utility.c */


char *Alloc(n)
int n;
{
  char *p;

  if ((p = (char *) malloc(n)) == 0)
    Barf("Out of space");

  return p;
}

Barf(s)
char *s;
{
  fprintf(stderr, "%s\n", s);
  exit(-1);
}

int EOF_Test(f)
FILE *f;
{
  char c, ReadChar();

  c = ReadChar(f);
  if (c == EOF)
    return TRUE;
  ungetc(c, f);
  return FALSE;
}


int SkipLine(f)
FILE *f;
{
  char c;

  do
    c = getc(f);
  while (c != EOF && c != '\n');

}

/* Skip whitespace */
Skip(f)
FILE *f;
{
  char c;
 
  while (isspace(c = getc(f)))
    ;
  ungetc(c,f);
}

   
/* Get a string terminated by whitespace */
int GetString(f, buff)
FILE *f;
char *buff;
{
  char c;

  Skip(f);
  while (!isspace(c = getc(f)))
    *buff++ = c;
  *buff = 0;
}

int Strcmp(s1, s2)
char *s1, *s2;
{
  while (*s1 && *s2){
    if (*s1++ != *s2++)
      return FALSE;
  }
  return *s1 == *s2;

}



StrAppend(s1, s2, s3)
char *s1, *s2, *s3;
{
  while (*s1)
    *s3++ = *s1++;
  while (*s2)
    *s3++ = *s2++;
  *s3 = 0;
}


int GetInt(f)
FILE *f;
{
  char c, ReadChar();
  int v, sign;

  c = ReadChar(f);
  sign = FALSE;
  v = 0;

  if (c == '-'){
    sign = TRUE;
    c = getc(f);
  }
  while (isdigit(c)){
    v = 10*v + (c - '0');
    c = getc(f);
  }
  if (sign)
    v = -1*v;

  ungetc(c, f);
  return v;
}

PutInt(i, f)
int i;
FILE *f;
{
  char c;
  int d;

  if (i == 0){
    putc('0', f);
    return;
  }

  if (i < 0){
    putc('-', f);
    i = -1*i;
  }
  
  d = 1;
  while (d <= i)
    d *= 10;
  d /= 10;
  while (d > 0){
    c = i / d;
    i %= d;
    d /= 10;
    putc('0'+c, f);
  }
  
}

char ReadChar(f)
FILE *f;
{
  char c;

  do {
    c = getc(f);
  } while (isspace(c));
  return c;
}

int Min(x, y)
int x, y;
{
  return (x > y) ? y : x;
}

int Max(x, y)
int x, y;
{
  return (x > y) ? x : y;
}

int Abs(x)
int x;
{
  return (x > 0) ? x : -x;
}

/* Open a file for reading - if the file doesn't exist,
then the extention .max is tried, if that doesn't work
then the data directory is checked - with or without the extention.
*/

FILE *OpenFile(c)
char *c;
{
  FILE *f;
  char buff1[100], buff2[100];

  if ((f = fopen(c,"r")) != NULL)
    return f;

  StrAppend(c,".max",buff1);
  if ((f = fopen(buff1,"r")) != NULL)
    return f;

  StrAppend(DATA_DIRECTORY,c,buff1);
  if ((f = fopen(buff1,"r")) != NULL)
    return f;

   StrAppend(buff1,".max",buff2);
   return fopen(buff2,"r");

  

}


Queue *MakeQueue(n)
int n;
{
  Queue *Q;

  Q = (Queue *) Alloc(sizeof(Queue));

  Q->data = (int *) Alloc(n * sizeof(int));
  Q->tail = 0;
  Q->head = 0;

  Q->size = n;

  return Q;
}

Dequeue(Q)
Queue *Q;
{
  int v;

  if (Q->tail == Q->head)
    Barf("Attempt to dequeue from empty queue");

  v = Q->data[Q->head];
  Q->head = (Q->head == Q->size - 1) ? 0 : Q->head + 1;

  return v;
}

Enqueue(Q, k)
Queue *Q;
int k;
{
  if (Q->head == Q->tail + 1 ||
      (Q->tail == Q->size - 1 && Q->head == 0))
    Barf("Queue overfull");

  Q->data[Q->tail] = k;
  Q->tail = (Q->tail == Q->size - 1) ? 0 : Q->tail + 1;
}

int QSize(Q)
Queue *Q;
{
  return (Q->tail >= Q->head) ? Q->tail - Q->head 
                              : Q->tail - Q->head + Q->size;
}

QueueEmpty(Q)
Queue *Q;
{
  return Q->head == Q->tail;
}
/* random.c -- functions dealing with randomization.

	RandomPermutation
        RandomInteger
        InitRandom
	RandomSubset
*/




/* RandomPermutation -- contruct a random permutation of the array perm.  
   It is assumed that the length of perm is n.  The algorithm used makes
   a pass through the array, randomly switching elements of the array.
*/
RandomPermutation (perm, n)
int perm[], n;
{
    int i, j, t;
			
    for (i = 0; i < n - 1; i++){
        j = RandomInteger(i, n-1);	/* Swap the element perm[i] with   */
	t = perm[i];			/* a random element from the range */
	perm[i] = perm[j];		/* i..n-1.			   */ 
	perm[j] = t;
    }
}

RandPerm (perm, n)
int perm[], n;
{
    int i, j, t;
	
    for (i = 0; i < n; i++)
        perm[i] = i;

    for (i = 0; i < n - 1; i++){
        j = RandomInteger(i, n-1);	/* Swap the element perm[i] with   */
	t = perm[i];			/* a random element from the range */
	perm[i] = perm[j];		/* i..n-1.			   */ 
	perm[j] = t;
    }
}



/* RandomInteger -- return a random integer from the range low .. high.
*/
int RandomInteger (low, high)
int low, high;
{
    return random() % (high - low + 1) + low;
}

/* InitRandom -- If the seed is non-zero, the random number generator is
   initialized with the seed, giving a fixed sequence of "random" numbers.
   If the seed is zero, then the time of day is used to intialize the random 
   number generator. 
*/
InitRandom (seed)
int seed;
{
    struct timeval tp;
   
    if (seed == 0){
        gettimeofday(&tp, 0);
        srandom(tp.tv_sec + tp.tv_usec);
    }
    else
	srandom(seed);
}

/* RandomSubset - return n distinct values, randomly selected between
high and low, the algorithm is inefficient if n is large - this could
be improved */
RandomSubset(low, high, n, x)
int low, high, n, *x;
{
  int i, j, r, flag;

  if (high - low + 1 < n)
    Barf("Invalid range for Random Subset");

  i = 0;
  while (i < n){
    r = RandomInteger(low, high);
    flag = 0;
    for (j = 0; j < i; j++)
      if (x[j] == r)
	flag = 1;
    if (flag == 0)
      x[i++] = r;
  }
  
}






/* io.c */


/* File Format

DIMACS format

c  Comment lines
p Problem Nodes Arcs   -----  problem: min, max, or asn
n id s/t               -----  source and sink designation
a src dst cap  

nodes in range 1 . . n


Solution graphs
c Comment lines
s Solution
f src dst flow

- note: internally, nodes are in the range 0 . . n-1

*/


Graph *InputFlowGraph(f, s, t)
FILE *f;
int *s, *t;
{
  char c, c1, ReadChar(), buff[100];
  Graph *G;
  int i, PType, nodes, edges, v, w, cap;

  G = (Graph *) malloc(sizeof(Graph));
  InitGraph(G);

  PType = UNDEFINED;
  
  while (1) {
    if (EOF_Test(f))
      break;
    c = ReadChar(f);
    switch (c) {

    case 'a':
      v = GetInt(f);
      w = GetInt(f);
      cap = GetInt(f);
      AddEdge(v - 1, w - 1, cap, G);
      break;

    case 'c':
      SkipLine(f);
      break;

    case 'n':
      if (PType == MAXFLOW){
	v = GetInt(f);
	c1 = ReadChar(f);
	if (c1 == 's')
	  *s = v - 1;
	else if (c1 == 't')
          *t = v - 1;
	else
	  Barf("Unexpected node type");
      }
      else {
	Barf("Unimplemented or undefined problem type");
      }
      break;

    case 'p':
      GetString(f, buff);
      if (Strcmp(buff, "max")){
	PType = MAXFLOW;
      }
      else
	Barf("Undefined problem type");
      nodes = GetInt(f);
      edges = GetInt(f);
      break;

    default:
      Barf("Unexpected case in InputFlowGraph\n");
      break;

    }
    
  }

  for (i = 0; i < nodes; i++)
    AddVertex(i, G);

  return G;
}


Graph *InputFlow(f, G, s)
FILE *f;
Graph *G;
int *s;
{
  char c, c1, ReadChar(), buff[100];
  int i, PType, nodes, edges, v, w, flow;
  Edge *e, *EdgeLookup();

  while (1) {
    if (EOF_Test(f))
      break;
    c = ReadChar(f);
    switch (c) {


    case 'c':
      SkipLine(f);
      break;

    case 'f':
      v = GetInt(f);
      w = GetInt(f);
      flow = GetInt(f);
      e = EdgeLookup(v-1,w-1,G);
      if (e == (Edge *) 0)
	Barf("Edge missing from graph");
      e->f += flow;
      e->mate -= flow;
      break;

    case 's':
      *s = GetInt(f);
      break;

    default:
      Barf("Unexpected case in InputFlow\n");
      break;

    }
    
  }

  return G;
}


int GraphOutput(G, f, s, t)
Graph *G;
int s, t;
FILE *f;
{
  int i;

  printf("p max %d %d\n", G->size, EdgeCount(G));
  printf("n %d s\n", s + 1);
  printf("n %d t\n", t + 1);
  for (i = 0; i <= G->max_v; i++){
    WriteVertex(i, G, f);
  }

}


int OutputFlow(G, f, s)
Graph *G;
int s;
FILE *f;
{
  int i;

  printf("s %d\n", s);
  for (i = 0; i <= G->max_v; i++){
    WriteVertex2(i, G, f);
  }

}

int PrintFlow(G, s)
Graph *G;
int s;
{
  OutputFlow(G, stdout, s);
}

PrintGraph(G)
Graph *G;
{
  int i;

  for (i = 0; i <= G->max_v; i++){
    if (G->V[i] == FALSE)
      continue;
    WriteVertex3(i, G, stdout);
  }
}


/* for file output */
int WriteVertex(v, G, f)
int v;
Graph *G;
FILE *f;
{
  Edge *e;

  e = G->A[v];
  while (e != (Edge *) 0){
    if (e->c > 0){
        printf("a %d %d %d\n", e->t + 1, e->h + 1, e->c);
    }
    e = e->next;
  }
}


int WriteVertex2(v, G, f)
int v;
Graph *G;
FILE *f;
{
  Edge *e;

  e = G->A[v];
  while (e != (Edge *) 0){
    if (e->f > 0){
        printf("f %d %d %d\n", e->t + 1, e->h + 1, e->f);
    }
    e = e->next;
  }
}

int WriteVertex3(v, G, f)
int v;
Graph *G;
FILE *f;
{
  Edge *e;

  e = G->A[v];
  while (e != (Edge *) 0){
    if (e->c > 0){
        printf("%d %d %d %d\n", e->t + 1, e->h + 1, e->c, e->f);
    }
    e = e->next;
  }
}



