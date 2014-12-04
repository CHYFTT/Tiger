#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void Tiger_gc ();

// The Gimple Garbage Collector.


//===============================================================//
// The Java Heap data structure.

/*
      ----------------------------------------------------
      |                        |                         |
      ----------------------------------------------------
      ^\                      /^
      | \<~~~~~~~ size ~~~~~>/ |
    from                       to
 */
struct JavaHeap
{
  int size;         // in bytes, note that this is for semi-heap size
  char *from;       // the "from" space pointer
  char *fromFree;   // the next "free" space in the from space
  char *to;         // the "to" space pointer
  char *toStart;    // "start" address in the "to" space
  char *toNext;     // "next" free space pointer in the to space
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init (int heapSize)
{
  // You should write 7 statement here:
  // #1: allocate a chunk of memory of size "heapSize" using "malloc"
  struct JavaHeap* jheap=(struct JavaHeap*)malloc(heapSize);

  // #2: initialize the "size" field, note that "size" field
  // is for semi-heap, but "heapSize" is for the whole heap.
    heap.size=heapSize/2;
  // #3: initialize the "from" field (with what value?)
    heap.from=(char*)jheap;
  // #4: initialize the "fromFree" field (with what value?)
    heap.fromFree=heap.from;
  // #5: initialize the "to" field (with what value?)
    heap.to=heap.fromFree+heap.size;
  // #6: initizlize the "toStart" field with NULL;
    heap.toNext=NULL;
  // #7: initialize the "toNext" field with NULL;
    heap.toStart=NULL;



    printf("Java Initial finished...\n");
    printf("Heap size:%d\n",heap.size);
    printf("Heap from:0x%d\n",heap.from);
    printf("Heap to:0x%d\n",heap.to);
  return;
}

// The "prev" pointer, pointing to the top frame on the GC stack.
// (see part A of Lab 4)
void *previous = 0;//Sum。java.c extern void* previous;



//===============================================================//
// Object Model And allocation


// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
      | vptr      ---|----> (points to the virtual method table)
      |--------------|
      | isObjOrArray | (0: for normal objects)
      |--------------|
      | length       | (this field should be empty for normal objects)
      |--------------|
      | forwarding   |
      |--------------|\
p---->| v_0          | \
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | v_{size-1}   | /e
      ----------------/
*/
// Try to allocate an object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1;
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new (void *vtable, int size)
{
  // Your code here:
	/*char* t=(char*)malloc(size);
		memset(t,0,size);
		*((int*)t)=(int*)vtable;
		return t;
*/
    if(heap.to-heap.fromFree<size)
    {

        printf("There is %d byte remained,but you need:%d\n",heap.to-heap.fromFree,size);
    	Tiger_gc();
    	 if(heap.to-heap.fromFree<size)
    	 {
    	      printf("Tiger_gc can not collecte enough space...\n");
	    	    exit(1);

    	 }




    }


    	printf("\nthis is Tiger_new\n");
    	printf("malloc size:%d\n",size);
        char* temp=heap.fromFree;
        *(temp+4)=0;
        *(temp+8)=NULL;
        *(temp+12)=0;
        heap.fromFree+=size;
        *((int*)temp)=(int*)vtable;

        printf("isObj=%d,address=:%d\n",*(temp+4),temp+4);
        printf("length=%d,address=:%d\n",*(temp+8),(temp+8));
        printf("forward=%d,address=:%d\n",*(temp+12),(temp+12));
        return temp;




}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
      | vptr         | (this field should be empty for an array)
      |--------------|
      | isObjOrArray | (1: for array)
      |--------------|
      | length       |
      |--------------|
      | forwarding   |
      |--------------|\
p---->| e_0          | \
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | e_{length-1} | /e
      ----------------/
*/
// Try to allocate an array object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this array object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1;
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new_array (int length)
{
  // Your code here:

/*	int *i=(int *)malloc(length*sizeof(int));
		i[0]=length;
		return i+1;
*/
	 if(heap.to-heap.fromFree<(length*sizeof(int))+16)
	    {
            printf("There is %d byte remained,but you need:%d\n",
             heap.to-heap.fromFree,length*(sizeof(int))+16);
	    	Tiger_gc();
	    	if(heap.to-heap.fromFree<(length*sizeof(int))+16)
	    	{
	    	    printf("Tiger_gc can not collecte enough space...\n");
	    	    exit(1);
	    	}


	    }




	    	printf("\nthis is Tiger_new_array\n");
	    	printf("malloc size:%d\n",length*(sizeof(int))+16);
	        char* temp=heap.fromFree;
	        *temp=NULL;
	        *(temp+4)=1;
	        *((int*)(temp+8))=length;
	        *(temp+12)=0;
	        heap.fromFree+=(length*sizeof(int));

	        printf("isObj=%d,address=:%d\n",*(temp+4),temp+4);
            printf("length=%d,address=:%d\n",*(temp+8),(temp+8));
            printf("forward=%d,address=:%d\n",*(temp+12),(temp+12));
	        return (temp+16);
}







//===============================================================//
// The Gimple Garbage Collector

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
int calculateSize(void* temp)
{
    int size=0;
    int* objAdd=*(int*)temp;
    printf("objorarray add is:0x%d\n",(char*)objAdd+4);
    int isArray=*((char*)objAdd+4);
    printf("isAarray is:%d\n",isArray);

    if(isArray)
    {
        //Array
        size=(*((char*)objAdd+8))*sizeof(int)+16;
        printf("return size is:%d\n",size);
        return size;
    }
    else
    {
        //Obj
        int len=0;
        int* vtable=*(int*)temp;
        char* classMap=*vtable;

        printf("----------------%d\n",classMap);

        len=strlen(classMap);

        printf("class arguments is:%d\n",len);

        size=len*4+16;

        printf("return size is:%d\n",size);

        return size;
    }

    return 0;
}




void* Copy(void *temp)
{
    //temp所指向的才是对象的位置,temp在frame里面
    //objAdd才是对象的位置
    int* objAdd=*(int*)temp;
    void* newAdd=temp;

    printf("heap to:%d\n",heap.from+heap.size);
    printf("heap from:%d\n",heap.from);


    if(((objAdd)<(heap.from+heap.size))&&(((objAdd)>=heap.from)))
    {//当目的地址在from区间里

        char* test=objAdd;

    printf("this is copy()-----------------------------\n");
    printf("objAdd is 0x%d\n",test);
    printf("-----------------------------------\n");
    printf("isObj=%d,address=:\n",*(test+4));
    printf("length=%d,address=:\n",*(test+8));
    printf("forward=%d,address=:\n",*(test+12));
    printf("-----------------------------------\n");



        void *forwarding =  ((char *)objAdd + 12);
        printf("forwarding is:%d\n",*(int*)forwarding);

        if((((int*)forwarding)<(heap.to+heap.size))&&((((int*)forwarding)>=heap.to)))
        {//forwarding在to区间里。说明已经copy。
             printf("forwarding is already in tospace!!!!!!!!!!!!!!!!:%d\n",*(int*)forwarding);
             return forwarding;

        }
        else if(((((int*)forwarding)<(heap.from+heap.size))&&((((int*)forwarding)>=heap.from)))
                ||((int*)forwarding)==0)
        {//forwarding在from区间里面或者forwarding为0

            printf("forwarding is in from or forwarding is 0\n");
            printf("forwarding is :%d\n",*(int*)forwarding);
            //因为没有给forwarding赋值，所以先用char*好输出0，不然是一大长串数。
            printf("heap.from+heap.size=0x%d ",heap.from+heap.size);
            printf("heap.from=0x%d   \n",heap.from);




            newAdd=heap.toNext;
            *(int*)forwarding=(int*)newAdd;
            printf("heap.toNext=0x%d\n",(int*)heap.toNext);
            printf("objAdd+12 = 0x%d\n",*(int**)((char *)objAdd + 12));
            printf("new forwarding is :0x%d\n",*(int*)forwarding);

            printf("Copy!!!!\n");
            //得到size。有obj的size与array的size两种情况。
            int size=calculateSize(temp);

            //开始copy
            //按字节copy
            int i=0;

            for(i=0;i<size;i++)
            {
                printf("heap.toNext is:0x%d\n",heap.toNext);

                *((char*)heap.toNext)=*((char*)objAdd+i);

                printf("from>>>>>%d   ",*((char*)objAdd+i));
                printf("to>>>>>%d\n",*((char*)heap.toNext));

                heap.toNext=(char*)heap.toNext+1;

            }
            return newAdd;
        }


        //copy finished...
        else
        {
            printf("obj not exit!!!\n");
            return 0;

        }
    }
    return temp;
}







 void Tiger_gc ()
{
  // Your code here:

  //
  heap.toStart=(char*)heap.to+1;
  heap.toNext=(char*)heap.to+1;
  printf("Tiger_gc start!\n");
  printf("heap.toStart=%d\n",(int*)heap.toStart);
  printf("heap.toNext=%d\n",(int*)heap.toNext);


  while(previous!=0)
  {
    char* arguments_gc_map = (*((char **)((char *)previous + 4)));
    //告诉编译器，这是一个指向指针的指针(char**)。
    //再用*修饰后，变为一个指针*(char**)。
    int* arguments_address=((int**)((char*)previous+8));
    int locals_gc_map=*(((char*)previous+12));

    printf("arguments_gc_map is:%s\n",arguments_gc_map);
    printf("arguments_gc_map address is:0x%d\n",*arguments_address);
    printf("locals_gc_map:%d\n",locals_gc_map);



    void* temp=0;
    //arguments
    if(arguments_gc_map!=0)
    {
        int* addr=arguments_address;
        int len=strlen(arguments_gc_map);
        int i=0;

        printf("%d\n",len);

        for(i=0;i<len;i++)
        {

            if(arguments_gc_map[i]=='1')
            {//字符用''
                temp=*((int**)addr);
                printf("in arguments print temp:0x%d\n",*(int**)temp);

                //Copy
                temp=Copy(temp);
                printf("copy finished...\n");


                addr=(char*)addr+4;


            }

        }
    }


     //locals
     if(locals_gc_map!=0)
     {
        int j=0;
        int* localStart=(int*)((char*)previous+16);
        int* localTemp=localStart;
        for(j=0;j<locals_gc_map;j++)
        {
            temp=(localTemp);
            printf("local temp is:%d\n",*(int*)temp);

            //Copy
            temp=Copy(temp);
            printf("\ncopy finished...\n");

            //每次向下移动一次
            localTemp=(char*)localTemp+4;

        }
     }

       previous = (char *)(*((char **)previous));
  }
  //previous遍历结束


  //交换heap和Free
    char *swap = heap.from;
	heap.from = heap.to;
	heap.to = swap;

	heap.fromFree = heap.toNext;

	return;




  //
}


