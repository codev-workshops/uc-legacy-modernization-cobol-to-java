package com.carddemo.common.dto;

public class DisclosureGroupDto {

    private String groupId;
    private String groupName;
    private String disclosureText;

    public DisclosureGroupDto() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDisclosureText() {
        return disclosureText;
    }

    public void setDisclosureText(String disclosureText) {
        this.disclosureText = disclosureText;
    }
}
