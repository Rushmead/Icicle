/*
 * MIT License
 *
 * Copyright (c) 2021 IceyLeagons and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.iceyleagons.icicle.core.translations.code.functions.impl.number;

import net.iceyleagons.icicle.core.translations.code.functions.AbstractCodeFunction;
import net.iceyleagons.icicle.core.translations.code.functions.CodeFunction;

@CodeFunction
public class ModulusFunction extends AbstractCodeFunction {

    public ModulusFunction() {
        super("MOD");
    }

    @Override
    public String parse(String input) {
        return super.handleSimpleList(input, s -> s != 2, (v1, v2) -> {
            if (!isInteger(v1) || !isInteger(v2)) return "error";
            return String.valueOf(parseInt(v1) % parseInt(v2));
        });
    }
}
