import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudentIO {

    //++++++++++++++++++++ Instance Variables +++++++++++++++++++++++++++

    private final int version = 1;
    private final String description = "A bunch of students";
    private String[] _fileNames;
    private HashMap<String, ArrayList<Student>> allLectures;
    private int numberOfStudents = 0;
    private SanityChecker sanityChecker;
    private String logFileName = "problem_students.txt";


    //+++++++++++++++++++++++++ Constructors ++++++++++++++++++++++++++
    public StudentIO(String[] fileNames){
        _fileNames = fileNames;
        allLectures = new HashMap<>();
        sanityChecker = new SanityChecker(logFileName);
        parseAll(_fileNames);
        produceStudentFile("students");
    }

    //++++++++++++++++++++++++++++++ METHODS ++++++++++++++++++++++++++++
    //=============================== parseAll =================================
    private void parseAll(String[] files){
        for( String fileName : files )
            allLectures.put( fileName, parseFromBBCSV( new File( fileName ) ) );
    }

    //=============================== parseFromCSV =================================
    private ArrayList<Student> parseFromBBCSV( File file ){

        BufferedReader fileScanner;
        try {
            fileScanner = new BufferedReader( new InputStreamReader(new FileInputStream(file), "Unicode"));
            ArrayList<Student> students = new ArrayList<>();

            // skip first line, it's junk
            String line = fileScanner.readLine();
            while( (line = fileScanner.readLine()) != null){
                Student s = parseBBLine(line, file.getName(),"\",\"");
                if( s != null )
                    students.add(s);
            }
            sanityChecker.close();
            return students;

        } catch (FileNotFoundException e){
            System.err.println( "File not found " + e.getMessage());
            System.exit(1);
        } catch( UnsupportedEncodingException io ){
            System.err.println( "File encoding not supported " + io.getMessage() );
            System.exit(1);
        } catch (IOException e ){
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    //=============================== parseLine =================================
    private Student parseBBLine( String line, String fileName, String delimiter ){

        String[] fields = line.split( delimiter );
        String name = fields[5] + " " + fields[2];
        String email = parseEmail(fields[8]);
        int year = resolveYear(fields[11].trim());

        ArrayList<String> goodTimes = new ArrayList<>();
        ArrayList<String> possibleTimes = new ArrayList<>();

        for( int i = 17; i < 93; i+=3 ){
            if( fields[i].equals("first choice") ){
                String choice =  fields[ i - 1 ];
                goodTimes.add( parseDayTime(choice) );
            }
            if( fields[i].equals("second choice")) {
                String choice =  fields[ i - 1 ];
                possibleTimes.add( parseDayTime(choice) );
            }
        }
        numberOfStudents++;

        Student thisStudent = new Student(name, email, fileName,"n/a", year, goodTimes, possibleTimes);
        sanityChecker.checkStudent( thisStudent );
        if( sanityChecker.addToRoster(thisStudent) )
            return thisStudent;
        else{
            System.out.println( thisStudent.getName() + " not added to roster. See " + logFileName );
            return null;
        }


    }

    public int resolveYear( String year ){
        switch (year){
            case "First-year":
                return 1;
            case "Sophomore":
                return 2;
            case "Junior":
                return 3;
            case "Senior":
                return 4;
            case "Grad or Special Status":
                return 5;
            default:
                return 0;
        }
    }

    //=========================== parseEmail() =================================
    private String parseEmail( String email ){
        Pattern wildcatEmailRegex = Pattern.compile("([a-zA-Z]{1,3}([0-9]+)?)");
        Matcher emailMatcher = wildcatEmailRegex.matcher(email);

        if( emailMatcher.find() && !email.contains("@") ) {
            email = emailMatcher.group();
            email += "@wildcats.unh.edu";
        }

        if( email.contains("<a href=\"\"mailto:")){
            int startIndex = email.indexOf( ">" ) + 1;
            email = email.substring(startIndex);
            int endIndex = email.indexOf("<");
            email = email.substring(0, endIndex);
        }

        if( email.contains("<br />")){
            email = email.replaceAll("<br />", "");
        }

        return email.trim();
    }

    //=========================== parseDayTime =======================================
    private String parseDayTime( String input ){

        Pattern timeFormat = Pattern.compile("((1[012]|[1-9]):[0-5][0-9])|([1-9])");
        Matcher matcher = timeFormat.matcher(input);
        String time1= "";
        String time2 ="";
        if( matcher.find() )
            time1 = matcher.group();
        if( matcher.find() )
            time2 = matcher.group();

        if( time1.length() == 1 )
            time1 = time1 + ":00";
        if( time2.length() == 1 )
            time2 = time2 + ":00";

        Pattern dayPattern = Pattern.compile("Mon|Tue|Wed|Thu|Fri");
        Matcher dayMatcher = dayPattern.matcher(input);
        String day = "";
        if( dayMatcher.find() )
            day = dayMatcher.group();
        return day + ": " + time1 + " - " + time2;
    }

    //=============================== produceStudentFile =================================
    private void produceStudentFile( String name ){

        try {
            File newFile = new File(name);
            if ( newFile.createNewFile()) {
                BufferedWriter fileOut = new BufferedWriter( new FileWriter( newFile ) );
                for( String s: _fileNames ){
                    printToFile( allLectures.get(s), fileOut );
                }
                fileOut.close();
                System.out.println("Student file written to " + name);
            }
            else
                overwrite(newFile);

        } catch (IOException io ){
            System.out.println( "StudentIO Exception " + io.getMessage() );
        }
    }

    //=============================== printToFile =================================
    private void printToFile( ArrayList<Student> students, Writer out ){
        try {
            out.append("Student Info Format: " + version + '\n');
            out.append("Description: " + description + '\n');
            out.append("Number of students: " + numberOfStudents + "\n");
            for (Student student : students) {
                out.append(student.print());
                //out.append('\n');
            }
        } catch (IOException io ){
            System.err.println( "IOException caught: " + io.getMessage() );
        }
    }


    //=============================== overwrite =================================
    private void overwrite( File file ){
        System.out.println("File " + file.getName() + " already exists: Overwrite (y/n) or Enter a new name.");
        Scanner stdIn = new Scanner( System.in );
        boolean properResponse = false;
        while( !properResponse ) {
            if (stdIn.hasNextLine()) {
                String response = stdIn.nextLine();
                if( response.length() == 1 ) {
                    switch (response.charAt(0)) {
                        case 'y':
                            deleteFile(file);
                            produceStudentFile(file.getName());
                            properResponse = true;
                            break;
                        case 'n':
                            System.out.println("File not overwritten. No changes made.");
                            properResponse = true;
                            break;
                        default:
                            properResponse = false;
                            System.out.println("Please type 'y' or 'n'.");
                            break;
                    }
                }
                else{
                    produceStudentFile(response);
                    properResponse = true;
                }
            }
        }
    }

    //=============================== deleteFile =================================
    private void deleteFile( File file ){
        if( file.delete() )
            System.out.println( file.getName() + " deleted successfully.");
        else
            System.out.println( file.getName() + " couldn't be deleted");
    }

    //===============================main=================================
    public static void main( String[] args ){
        new StudentIO(args);
    }
}
