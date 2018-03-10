package ca.uqac.inf135.group3.project.model.entities.goasp;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

@DatabaseTable(tableName = "scope")
public class GoaspScope {

    public static String[] splitScopeString(String scope) {
        if (scope != null) {
            return scope.toLowerCase().split(" ");
        }
        return new String[0];
    }

    @DatabaseField(id = true)
    private String name;

    @DatabaseField(canBeNull = true, foreign = true)
    private GoaspSinglePageApp app;

    @DatabaseField()
    private String description;

    @DatabaseField()
    private boolean isPublic;

    //Dao constructor
    public GoaspScope() {
    }

    //Data constructor
    public GoaspScope(String name, GoaspSinglePageApp app, String description, boolean isPublic) {
        this.app = app;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }
    public GoaspScope(String name, GoaspSinglePageApp app, String description) {
        this(name, app, description, false);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public GoaspSinglePageApp getApp() {
        return app;
    }

    public void setApp(GoaspSinglePageApp app) {
        this.app = app;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
