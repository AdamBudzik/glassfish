/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.jdbc.devtests.v3.test.reconfig;

import java.io.*;
import java.net.*;

import java.util.Map;
import jakarta.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

/**
 *
 * @author shalini
 */
public class ReconfigTestServlet extends HttpServlet {
    @Resource(name = "jdbc/jdbc-dev-test-resource", mappedName = "jdbc/jdbc-dev-test-resource")
    DataSource ds;
    
    @Resource(name = "jdbc/jdbc-reconfig-test-resource-1", mappedName = "jdbc/jdbc-reconfig-test-resource-1")
    DataSource dsReconfig1;

    @Resource(name = "jdbc/jdbc-reconfig-test-resource-2", mappedName = "jdbc/jdbc-reconfig-test-resource-2")
    DataSource dsReconfig2;

    @Resource(name = "jdbc/res1", mappedName = "jdbc/res1")
    DataSource dsRes1;
    
    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        processRequest(arg0, arg1);
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ReconfigTestUtil reconfigTest = new ReconfigTestUtil();
        StringBuffer buf = new StringBuffer();
        Map<String, Boolean> mapReconfig = null;
        //Determine the test to be executed
        int testId = Integer.parseInt(request.getParameter("testId").trim());
        boolean throwException = Boolean.parseBoolean(request.getParameter("throwException").trim());
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>ReconfigTestServlet</title>");
            out.println("</head>");
            out.println("<body>");

            switch (testId) {
                case 1: //Attribute change test

                    int maxPoolSize = Integer.parseInt(request.getParameter("maxPoolSize").trim());
                    out.println("<h1>Reconfig Pool Attribute Test </h1>");
                    mapReconfig = reconfigTest.poolAttributeChangeTest(ds, out, maxPoolSize, throwException);
                    break;
                case 2: //Property change test

                    out.println("<h1>Reconfig Pool Property Test </h1>");
                    mapReconfig = reconfigTest.poolPropertyChangeTest(ds, out, throwException);
                    break;
                case 3: //Resource attribute change test

                    out.println("<h1>Reconfig Resource Attribute Test with DS : dsReconfig2</h1>");
                    mapReconfig = reconfigTest.resourceAttributeChangeTest(dsReconfig2, out, throwException);
                    break;
                case 4: //Resource attribute change test with another datasource
                    
                    out.println("<h1>Reconfig Resource Attribute Test with DS : dsRes1</h1>");
                    mapReconfig = reconfigTest.resourceAttributeChangeTest(dsRes1, out, throwException);
                    break;
            }
            buf.append("<table border=1><tr><th>Test Name</th><th> Pass </th></tr>");
            for (Map.Entry entry : mapReconfig.entrySet()) {
                buf.append("<tr> <td>");
                buf.append(entry.getKey());
                buf.append("</td>");
                buf.append("<td>");
                buf.append(entry.getValue());
                buf.append("</td></tr>");
            }
            buf.append("</table>");
            out.println(buf.toString());

            out.println("</body>");
            out.println("</html>");

        } finally {
            out.close();
            out.flush();
        }
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Tests the reconfiguration changes to JDBC Connection Pool and " +
                "JDBC Resource";
    }

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

}
