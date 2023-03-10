import java.util.Scanner;
import java.time.Duration;
import java.time.Instant;

public class matrixproduct{

  static void OnMult(int m_ar, int m_br){
    double temp;
    int i, j, k;
    Instant Time1, Time2;

    double[] pha = new double[m_ar * m_ar];
    double[] phb = new double[m_ar * m_ar];
    double[] phc = new double[m_ar * m_ar];

    for(i=0; i<m_ar; i++)
      for(j=0; j<m_ar; j++)
        pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
      for(j=0; j<m_br; j++)
        phb[i*m_br + j] = (double)(i+1); 
        
    Time1 = Instant.now();   
        
    for(i=0; i<m_ar; i++)
      for(j=0; j<m_br; j++){
        temp = 0;
        for(k=0; k<m_ar; k++){
          temp += pha[i*m_ar+k] * phb[k*m_br+j];
        }
        phc[i*m_ar+j]=temp;
      }

    Time2 = Instant.now();

    Duration time = Duration.between(Time1, Time2);
    
    System.out.println("Time: " + time + "seconds");  

    System.out.println("Result matrix: ");
    for(i=0; i<1; i++)
      for(j=0; j<Math.min(10, m_br); j++)
        System.out.println(phc[j] + " ");      
  }

  static void OnMultLine(int m_ar, int m_br){
    int i, j, k;
    Instant Time1, Time2;

    double[] pha = new double[m_ar * m_ar];
    double[] phb = new double[m_ar * m_ar];
    double[] phc = new double[m_ar * m_ar];

    for(i=0; i<m_ar; i++)
      for(j=0; j<m_ar; j++)
        pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
      for(j=0; j<m_br; j++)
        phb[i*m_br + j] = (double)(i+1);   
        
    Time1 = Instant.now();      
          
    for (i = 0; i < m_ar; i++) {	
      for (k = 0; k < m_ar; k++) {
        for (j = 0; j < m_br; j++) {
            phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
        }
      }
    }

    Time2 = Instant.now();

    Duration time = Duration.between(Time1, Time2);
    
    System.out.println("Time: " + time + "seconds"); 
  
    System.out.println("Result matrix: ");
    for(i=0; i<1; i++)
      for(j=0; j<Math.min(10, m_br); j++)
        System.out.println(phc[j] + " ");      
  }

  static void OnMultBlock(int m_ar, int m_br, int bkSize){
    double temp;
    int i, j, k, ii, jj, kk;

    double[] pha = new double[m_ar * m_ar];
    double[] phb = new double[m_ar * m_ar];
    double[] phc = new double[m_ar * m_ar];

    for(i=0; i<m_ar; i++)
      for(j=0; j<m_ar; j++)
        pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
      for(j=0; j<m_br; j++)
        phb[i*m_br + j] = (double)(i+1);    
        
    for (jj=0	; jj<m_ar; jj+=bkSize){
      for (kk=0; kk<m_br; kk+=bkSize){
        for(i=0; i<m_ar; i++){
          for(j=jj; j<((jj+bkSize)>m_ar?bkSize:(jj+bkSize)); j++){
            temp = 0;
            for(k=kk; k<((kk+bkSize)>m_ar?bkSize:(kk+bkSize)); k++){
              temp += pha[i*m_ar+k] * phb[k*m_br+j];
            }
            phc[i*m_ar+j] += temp;
          }
        }
      }
    }

    System.out.println("Result matrix: ");
    for(i=0; i<1; i++)
      for(j=0; j<Math.min(10, m_br); j++)
        System.out.println(phc[j] + " ");  
  }

  public static void main(String[] args) {
    
    int lin, col, blockSize;
    int op=1;
    Scanner stdin = new Scanner(System.in);

    do{
      System.out.println("1. Multiplication");
      System.out.println("2. Line Multiplication");
      System.out.println("3. Block Multiplication");
      System.out.println("Selection?: ");
      op = stdin.nextInt();

      if(op == 0) break;
      
      System.out.println("Dimensions: lins=cols ? ");
      lin = stdin.nextInt();
      col = lin;

      switch(op) {
        case 1:
          OnMult(lin, col);
          break;
        case 2:
          OnMultLine(lin, col);
          break;
        case 3:
          System.out.println("Block Size? ");
          blockSize = stdin.nextInt();
          OnMultBlock(lin, col, blockSize);  
          break;
      }
    } while(op!=0);

    stdin.close();
  }
}