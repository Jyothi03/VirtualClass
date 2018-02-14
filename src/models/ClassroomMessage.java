package models;

import java.io.Serializable;

public class ClassroomMessage implements Serializable {
    private String personId;
    private long timestamp; 
    private String type;
    private String content;
    


    public ClassroomMessage(String personId, long timestamp, String type, String content) {
        this.personId = personId;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String toString() {
        return ("[" + getPersonId() + "] : " + getTimestamp() + " : " + getType() + " : " + getContent());
    }

    /**
     * @return the personId
     */
    public String getPersonId() {
        return personId;
    }

    /**
     * @param personId the personId to set
     */
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
}
