package datatypes;

import java.io.Serializable;

public class Rule implements Serializable {

    public String sqlQuery;

    public Rule(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }


}
