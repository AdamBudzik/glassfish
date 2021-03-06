/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.beans;

import jakarta.enterprise.context.RequestScoped;

import test.beans.qualifiers.Preferred;
import test.beans.stereotypes.SecStereotype;

@SecStereotype
@RequestScoped
@Preferred
public class TestSecureBean implements TestBeanInterface{
    public static boolean testBeanInvoked = false;

    @Override
    public void m1() {
        testBeanInvoked = true;
        System.out.println("TestBean::m1 called");
    }

    @Override
    public void m2() {
        System.out.println("TestBean::m2 called");
    }

}
