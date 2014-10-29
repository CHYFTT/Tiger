class Sum { 
	public static void main(String[] a) {
        System.out.println(new Doit().doit(101));
    }
}

class Doit {
    public int doit(int n) {
        int sum;
        int i;
        int j[10];
        
        i = 0;
        sum = 0;
        while (i<n){
        	sum = sum + i;
        	i = i+1;
        }
        return sum;
    }
}
