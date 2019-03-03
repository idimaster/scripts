/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.scripts;

import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExpressionCompiler;
import org.openjdk.jmh.annotations.Benchmark;

import javax.script.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mvel2.MVEL;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;


@State(Scope.Thread)
public class ScriptBenchmark {

    private List<Account> accounts = new ArrayList<Account>() {{
        add(new Account(34, "Checking", "my"));
        add(new Account(347, "Checking", "my"));
        add(new Account(94, "Checking", "my"));
    }};

    private static String code = "count = 0; " +
            "sum = 0; " +
            "foreach(account : accounts) { " +
            "  if(account.type == 'Checking') { " +
            "    count++;" +
            "    sum += account.balance;" +
            "  } " +
            "} " +
            "return sum/count;";

    private static CompiledExpression expression = new ExpressionCompiler(code).compile();

    @Benchmark
    public void MVELFindCheckingAverage() {
        Map vars = new HashMap();
        vars.put("accounts", accounts);
        Double output = (Double) MVEL.eval(code, vars);
    }

    @Benchmark
    public void MVELCompiledFindCheckingAverage() {
        Map vars = new HashMap();
        vars.put("accounts", accounts);
        Double output = (Double) MVEL.executeExpression(expression, vars);
    }

    @Benchmark
    public void JavaScriptTestFindCheckingAverage() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        Invocable invocable = (Invocable) engine;

        String statement = "function average(accounts) {" +
                " var sum = 0.0;" +
                " var count = 0;" +
                " for each(account in accounts) { " +
                "   if(account.getType() === 'Checking') { " +
                "     sum += account.getBalance();" +
                "     count++;" +
                "    } " +
                "  } " +
                " return sum / count; " +
                "} ";
        CompiledScript cs = ((Compilable) engine).compile(statement);
        cs.eval();
        Double output = (Double) invocable.invokeFunction("average", accounts);
    }

    @Benchmark
    public void testMethod() {
        int sum = 0;
        int count = 0;
        for(Account a : accounts) {
            if ("Checking".equals(a.getType())) {
                sum += a.getBalance();
                count++;
            }
        }
        double output = (double)sum / count;
    }
}