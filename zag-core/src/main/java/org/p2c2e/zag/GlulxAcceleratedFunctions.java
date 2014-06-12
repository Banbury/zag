package org.p2c2e.zag;

//ported from accel.c from Andrew Plotkin's glulx

abstract class InformFuncs implements AcceleratedFunction {
    Zag z;

    public InformFuncs(Zag z) {
        this.z = z;
    }

    public static int arg_if_given(int [] args, int numargs, int idx) {
        if (numargs > idx) {
            return args[idx];
        } else {
            return 0;
        }
    }

    public int zregion(int raddr) {
        if (raddr < 36) {
            return 0;
        }
        int endmem = z.getMemSize();
        if (raddr >= endmem) {
            return 0;
        }

        int tb = ((int)z.memory.get(raddr)) & 0xff;
        if (tb >= 0xE0) {
            return 3;
        }
        if (tb >= 0xC0) {
            return 2;
        }
        if (tb >= 0x70 && tb <= 0x7F && raddr >= z.ramstart) {
            return 1;
        }

        return 0;
    }

    int get_prop(int obj, int id)
    {
        int cla = 0;
        int [] call_args = new int[2];

        if ((id & 0xFFFF0000) != 0) {
            cla = z.memory.getInt(z.classes_table + ((id & 0xFFFF) * 4));
            call_args[0] = obj;
            call_args[1] = cla;

            if (oc_cl(2, call_args) == 0)
                return 0;

            id >>>= 16;
            obj = cla;
        }

        call_args[0] = obj;
        call_args[1] = id;
        int prop = cptab(2, call_args);
        if (prop == 0)
            return 0;

        if (obj_in_class(obj) && (cla == 0)) {
            if ((id < z.indiv_prop_start) || (id >= z.indiv_prop_start+8))
                return 0;
        }

        if (z.memory.getInt(z.self) != obj) {
            if ((z.memory.get(prop + 9) & 1) != 0)
                return 0;
        }
        return prop;
    }

    int oc_cl(int numargs, int[] args) {

        int obj = arg_if_given(args, numargs, 0);
        int cla = arg_if_given(args, numargs, 1);

        int zr = zregion(obj);
        if (zr == 3)
            return (cla == z.string_metaclass) ? 1 : 0;
        if (zr == 2)
            return (cla == z.routine_metaclass) ? 1 : 0;
        if (zr != 1)
            return 0;

        if (cla == z.class_metaclass) {
            if (obj_in_class(obj))
                return 1;
            if (obj == z.class_metaclass)
                return 1;
            if (obj == z.string_metaclass)
                return 1;
            if (obj == z.routine_metaclass)
                return 1;
            if (obj == z.object_metaclass)
                return 1;
            return 0;
        }
        if (cla == z.object_metaclass) {
            if (obj_in_class(obj))
                return 0;
            if (obj == z.class_metaclass)
                return 0;
            if (obj == z.string_metaclass)
                return 0;
            if (obj == z.routine_metaclass)
                return 0;
            if (obj == z.object_metaclass)
                return 0;
            return 1;
        }
        if ((cla == z.string_metaclass) || (cla == z.routine_metaclass))
            return 0;

        if (!obj_in_class(cla)) {
            System.out.println("[** Programming error: tried to apply 'ofclass' with non-class **]");
            return 0;
        }

        int prop = get_prop(obj, 2);
        if (prop == 0)
            return 0;

        int inlist = z.memory.getInt(prop + 4);
        if (inlist == 0)
            return 0;

        int inlistlen = z.memory.getShort(prop + 2);
        for (int jx = 0; jx < inlistlen; jx++) {
            if (z.memory.getInt(inlist + (4 * jx)) == cla)
                return 1;
        }
        return 0;
    }

    int cptab(int numargs, int [] args) {
        int obj = arg_if_given(args, numargs, 0);
        int id = arg_if_given(args, numargs, 1);

        if (zregion(obj) != 1) {
            //error
            System.out.println("Error: bad args");
            return 0;
        }

        int otab = z.memory.getInt(obj + 16);
        if (otab == 0) return 0;

        int max = z.memory.getInt(otab);
        return z.binarySearch(id, 2, otab + 4, 10, max, 0, 0);
    }


    boolean obj_in_class(int obj)
    {
        /* This checks whether obj is contained in Class, not whether
           it is a member of Class. */
        return (z.memory.getInt(obj + 13 + z.num_attr_bytes) == z.class_metaclass);
    }

    int ra_pr(int numargs, int[] args) {
        int obj = arg_if_given(args, numargs, 0);
        int id = arg_if_given(args, numargs, 1);

        int prop = get_prop(obj, id);
        if (prop == 0)
            return 0;
        
        return z.memory.getInt(prop + 4);
    }
}

final class Func1ZRegion extends InformFuncs {

    public Func1ZRegion(Zag z) {
        super(z);
    }

    public int enterFunction(int numargs, int[] args) {
        return zregion(args[0]);
    }

}

final class Func2CPTab extends InformFuncs {

    public Func2CPTab(Zag z) {
        super(z);
    }

    public int enterFunction(int numargs, int[] args) {
        return cptab(numargs, args);
    }
}

final class Func3RAPr extends InformFuncs {

    public Func3RAPr(Zag z) {
        super(z);
    }

    public int enterFunction(int numargs, int[] args) {
        return ra_pr(numargs, args);
    }

}

final class Func4RLPr extends InformFuncs {
    public Func4RLPr(Zag z) {
        super(z);
    }
    public int enterFunction(int numargs, int[] args) {
        int obj = arg_if_given(args, numargs, 0);
        int id = arg_if_given(args, numargs, 1);

        int prop = get_prop(obj, id);
        if (prop == 0)
            return 0;

        return 4 * z.memory.getShort(prop + 2);
    }
}

final class Func5OCCl extends InformFuncs {
    public Func5OCCl(Zag z) {
        super(z);
    }

    public int enterFunction(int numargs, int[] args) {
        return oc_cl(numargs, args);
    }
}

final class Func6RVPr extends InformFuncs {
    public Func6RVPr(Zag z) {
        super(z);
    }
    public int enterFunction(int numargs, int[] args) {

        int id = arg_if_given(args, numargs, 1);

        int addr = ra_pr(numargs, args);

        if (addr == 0) {
            if ((id > 0) && (id < z.indiv_prop_start))
                return z.memory.getInt(z.cpv_start + (4 * id));

            System.out.println("[** Programming error: tried to read (something) **]");
            return 0;
        }

        return z.memory.getInt(addr);
    }
}

final class Func7OpPr extends InformFuncs {
    public Func7OpPr(Zag z) {
        super(z);
    }
    
    public int enterFunction(int numargs, int[] args) {

        int obj = arg_if_given(args, numargs, 0);
        int id = arg_if_given(args, numargs, 1);

        int zr = zregion(obj);
        if (zr == 3) {
            /* print is INDIV_PROP_START+6 */
            if (id == z.indiv_prop_start+6)
                return 1;
            /* print_to_array is INDIV_PROP_START+7 */
            if (id == z.indiv_prop_start+7)
                return 1;
            return 0;
        }
        if (zr == 2) {
            /* call is INDIV_PROP_START+5 */
            return ((id == z.indiv_prop_start+5) ? 1 : 0);
        }
        if (zr != 1)
            return 0;

        if ((id >= z.indiv_prop_start) && (id < z.indiv_prop_start+8)) {
            if (obj_in_class(obj))
                return 1;
        }

        return ((ra_pr(numargs, args) != 0) ? 1 : 0);
    }
    
}

