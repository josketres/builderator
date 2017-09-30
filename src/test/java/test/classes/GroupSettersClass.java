package test.classes;

import java.util.Date;

public class GroupSettersClass {
    private int id;
    private Date beginValidity;
    private Date endValidity;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getBeginValidity() {
        return beginValidity;
    }

    public void setBeginValidity(Date beginValidity) {
        this.beginValidity = beginValidity;
    }

    public Date getEndValidity() {
        return endValidity;
    }

    public void setEndValidity(Date endValidity) {
        this.endValidity = endValidity;
    }
}
