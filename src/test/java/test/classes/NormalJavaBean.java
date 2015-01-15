package test.classes;

import java.util.Date;

public class NormalJavaBean {
    private String name;
    private int age;
    private Date date;
    private Address address;

    public NormalJavaBean() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
    
    public String getSuperName() {
        return "super" + getName();
    }
}
