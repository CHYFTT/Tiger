class Sum { 
	public static void main(String[] a) {
        System.out.println(new Doit().doit(101));
    }
}

class Doit {
	int k;
	int p;
    public int doit(int n) {
        int sum;
        int i;
        int[] j;
        
        
        i = 0;
        sum = 0;
        j=new int[10];
        i=j.length;
        j[1]=1;
        System.out.println(j[1]);
        while (i<n){
        	sum = sum + i;
        	i = i+1;
        }
        return sum;
    }
}
