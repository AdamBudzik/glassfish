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

package ejb32.methodintf;

import javax.ejb.*;
import javax.interceptor.*;
import jakarta.annotation.*;

@Stateful
public class StfulEJB implements St {

    private static boolean pc = false;
    private static boolean intf = false;
    private String caller = null;

    @PostConstruct
    public void test() {
        if (caller == null) {
            System.out.println("In StfulEJB: test LC");
            pc = Verifier.verify_tx(true);
        } else if (caller.equals("intf")) {
            System.out.println("In StfulEJB: test remote");
            intf = Verifier.verify_tx(false);
        }
        caller = null;
    }

    @AroundInvoke
    private Object around_invoke(InvocationContext ctx) throws Exception {
        caller = "intf";
        return ctx.proceed();
    }

    public boolean verify() {
        return pc && intf;
    }

}
