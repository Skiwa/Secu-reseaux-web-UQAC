package ca.uqac.inf135.group3.project.tools.stsp;

import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.html.HTML;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

//NOTE: Stsp stands for Simple Tag Substitution Page
public class StspFile {
    private static final String BASE_PATH = "res/stsp";

    private static String getFullPath(String relativePath) {
        return String.format("%s/%s", BASE_PATH, relativePath);
    }

    private final String relativePath;
    private final List<Object> parts = new LinkedList<>();

    public StspFile(String relativePath) {
        this.relativePath = relativePath;

        loadPage();
    }

    private void loadPage() {
        String fullPath = getFullPath(relativePath);

        try {
            FileInputStream inputStream = new FileInputStream(fullPath);

            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String fileContent = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            //Parse fileContent to extract Substitution Tags
            int lastTagPos = 0;
            int tagPos;
            while ((tagPos = fileContent.indexOf("{{", lastTagPos)) >= 0) {
                final int endTagPos = fileContent.indexOf("}}", tagPos);

                if (endTagPos > tagPos) {
                    //Add literal string between the last tag and the new one
                    if (tagPos > lastTagPos) {
                        parts.add(fileContent.substring(lastTagPos, tagPos));
                    }

                    //Lookout the new tag name
                    String tag = fileContent.substring(tagPos+2, endTagPos);
                    if (tag.length() > 0) {
                        parts.add(new StspTag(tag));
                    }

                    lastTagPos = endTagPos + 2;
                }
                else {
                    lastTagPos = fileContent.length();
                }
            }

            //Add the rest too
            if (fileContent.length() > lastTagPos) {
                parts.add(fileContent.substring(lastTagPos));
            }

        } catch (FileNotFoundException e) {
            parts.clear();
            System.err.println("An error occurred reading file " + fullPath);
            e.printStackTrace();
        }
    }

    private void set(String tag, Object value) {
        for(Object part : parts) {
            if (part instanceof StspTag) {
                StspTag subst = (StspTag) part;

                if (subst.match(tag)) {
                    subst.setValue(value);
                }
            }
        }
    }

    public void setStsp(String tag, StspFile stsp) {
        set(tag, stsp);
    }

    public void setHTML(String tag, String value) {
        set(tag, value != null ? ExchangeHelper.escapeHTML(value) : "");
    }

    public void setHTML(String tag, HTML html) {
        set(tag, html);
    }

    public List<StspTag> getTagList() {
        final LinkedList<StspTag> tags = new LinkedList<>();

        for (Object part : parts) {
            if (part instanceof StspTag) {
                tags.add((StspTag) part);
            }
        }

        return tags;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(Object part : parts) {
            if (part != null) {
                sb.append(part.toString());
            }
        }

        return sb.toString();
    }
}
