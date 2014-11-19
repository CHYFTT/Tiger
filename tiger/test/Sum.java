class Sum { 
	public static void main(String[] a) {
        System.out.println(new Doit().doit(101));
    }
}

class Doit {
    public int doit(int n) {
        int sum;
        int i;
       // DeadClass d;
        
        i = 0;
        sum = 0;
        while (i<n){
        	sum = sum + i;
        	i = i+1;
        }
        return sum;
    }
}

class DeadClass{
	int i;
	int j;
	public int deadMethod(int n){
		return 1;
	}
}
