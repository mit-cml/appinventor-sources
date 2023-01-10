import java.util.Arrays;
public class IteratingArray {
   public static void main(String args[]) {
      //Creating an array
      int myArray[] = new int[7];
      //Populating the array
      myArray[0] = 12;
      myArray[1] = 14;
      myArray[2] = 5;
      myArray[3] = 14;
      myArray[4] = 5;
      myArray[5] = 5;
      myArray[6] = 7;
      //Printing the element
      System.out.println("Contents of the array: ");
      for (int element: myArray) {
         System.out.println(element);
      }
   }
}
