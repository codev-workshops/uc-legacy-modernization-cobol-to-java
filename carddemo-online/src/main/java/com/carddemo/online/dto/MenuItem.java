package com.carddemo.online.dto;

public class MenuItem {

    private int option;
    private String name;
    private String programId;

    public MenuItem() {
    }

    public MenuItem(int option, String name, String programId) {
        this.option = option;
        this.name = name;
        this.programId = programId;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }
}
