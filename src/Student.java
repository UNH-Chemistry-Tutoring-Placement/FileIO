import java.util.ArrayList;

public class Student {

    private int _year;
    private String _name, _email, _lectureTime, _sex;
    private ArrayList<String> _goodTimes, _possibleTimes;

    public Student( String name, String email, String lectureTime, String sex, int year, ArrayList<String> goodTimes, ArrayList<String> possibleTimes ){
        _name = name;
        _email = email;
        _lectureTime = lectureTime;
        _sex =sex;
        _year = year;
        _goodTimes = goodTimes;
        _possibleTimes = possibleTimes;
    }

    public String print(){

        StringBuilder builder = new StringBuilder();

        builder.append("Name: ").append( _name ).append('\n')
                .append("Email: ").append( _email ). append('\n')
                .append("Lecture: ").append(_lectureTime).append('\n')
                .append("Year: ").append(_year).append('\n')
                .append("Sex: "). append(_sex).append('\n')
                .append("Number of good times: "). append(_goodTimes.size()).append('\n');

        for( String s: _goodTimes){
            builder.append(s).append('\n');
        }
        builder.append("Number of possible times: "). append(_possibleTimes.size()).append('\n');

        for( String s: _possibleTimes ){
            builder.append(s).append('\n');
        }
        return builder.toString();

    }
}
