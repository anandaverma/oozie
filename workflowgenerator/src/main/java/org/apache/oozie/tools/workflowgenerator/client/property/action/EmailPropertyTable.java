package org.apache.oozie.tools.workflowgenerator.client.property.action;

import org.apache.oozie.tools.workflowgenerator.client.property.PropertyTable;
import org.apache.oozie.tools.workflowgenerator.client.widget.NodeWidget;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

/**
 * Class for property table of email action
 */
public class EmailPropertyTable extends PropertyTable {

    private TextBox to;
    private TextBox cc;
    private TextBox subject;
    private TextBox body;

    /**
     * Constructor which records node widget and initializes
     *
     * @param w node widget
     */
    public EmailPropertyTable(NodeWidget w) {
        super(w);
        initWidget();
    }

    /**
     * Generate xml elements of email action and attach them to xml doc
     */
    public void generateXML(Document doc, Element root, NodeWidget next) {

        Element action = doc.createElement("action");
        action.setAttribute("name", current.getName());

        // create <email>
        Element emailEle = doc.createElement("email");
        action.appendChild(emailEle);

        // create <to>
        emailEle.appendChild(generateElement(doc, "to", to));

        // create <cc>
        emailEle.appendChild(generateElement(doc, "cc", cc));

        // create <subject>
        emailEle.appendChild(generateElement(doc, "subject", subject));

        // create <body>
        emailEle.appendChild(generateElement(doc, "body", body));

        // create <ok>
        action.appendChild(generateOKElement(doc, next));

        // create <error>
        action.appendChild(generateErrorElement(doc));

        root.appendChild(action);
    }

    /**
     * Initialize widgets shown in property table
     */
    protected void initWidget() {

        grid = new Grid(7, 2);
        this.add(grid);

        this.setAlwaysShowScrollBars(true);
        this.setSize("100%", "80%");

        // insert row for Name
        name = insertTextRow(grid, 0, "Name");

        // insert row for OK
        insertOKRow(grid, 1);

        // insert row for ERROR
        insertErrorRow(grid, 2);

        // insert row for TO
        to = insertTextRow(grid, 3, "To");

        // insert row for CC
        cc = insertTextRow(grid, 4, "CC");

        // insert row for Subject
        subject = insertTextRow(grid, 5, "Subject");

        // insert row for Body
        body = insertTextRow(grid, 6, "Body");
    }
}
