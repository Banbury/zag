package org.p2c2e.zag;

final class HuffmanTree
{
  static final byte NON_LEAF = 0x00;
  static final byte TERM  = 0x01;
  static final byte CHAR = 0x02;
  static final byte C_STRING = 0x03;
  static final byte UNICODE_CHAR = 0x04;
  static final byte UNICODE_STRING = 0x05;
  static final byte IND_REF = 0x08;
  static final byte DIND_REF = 0x09;
  static final byte IND_REFA = 0x0a;
  static final byte DIND_REFA = 0xb;
  

  Node root;
  int startaddr;

  HuffmanTree(Zag z, int address)
  {
    int len = z.memory.getInt(address);
    int rootaddr = z.memory.getInt(address + 8);
    
    startaddr = address;
    int endaddr = startaddr + len;

    if (rootaddr < z.endmem && endaddr < z.ramstart)
      root = readTree(z, rootaddr, true);
    else
      root = null;
  }

  Node readTree(Zag z, int rootaddress, boolean recurse)
  {
    Node n = new Node();
    n.type = z.memory.get(rootaddress);


    switch(n.type)
    {
    case TERM:
      break;
    case CHAR:
      n.c = z.memory.get(rootaddress + 1);
      break;
    case UNICODE_CHAR:
      n.u = z.memory.getInt(rootaddress + 1);
      break;
    case C_STRING:
      int len = 0;
      n.addr = rootaddress + 1;
      while (z.memory.get(n.addr + len) != 0)
        len++;
      n.numargs = len;
      break;
    case UNICODE_STRING:
      len = 0;
      n.addr = rootaddress + 1;
      while (z.memory.getInt(n.addr + len * 4) != 0)
          len ++;
      n.numargs = len;
      break;
    case IND_REF:
    case DIND_REF:
      n.addr = z.memory.getInt(rootaddress + 1);
      break;
    case IND_REFA:
    case DIND_REFA:
      n.addr = z.memory.getInt(rootaddress + 1);
      n.numargs = z.memory.getInt(rootaddress + 5);
      n.args = new int[n.numargs];
      z.memory.position(rootaddress + 9);
      for (int i = 0; i < n.numargs; i++)
        n.args[i] = z.memory.getInt();
      break;
    default:
      n.leftaddr = z.memory.getInt(rootaddress + 1);
      n.rightaddr = z.memory.getInt(rootaddress + 5);
      if (recurse)
      {
        n.left = readTree(z, n.leftaddr, recurse);
        n.right = readTree(z, n.rightaddr, recurse);
      }
    }

    return n;
  }

  static final class Node
  {
    byte type;
    Node left, right;
    byte c;
    int u;
    int addr;
    int numargs;
    int[] args;
    int leftaddr, rightaddr;

    public String toString() {
        switch(type) {
        case 0:
            return "";
        case TERM:
            return "TERM";
        case CHAR:
            return "(c " + ((char)c) + ")";
        case UNICODE_CHAR:
            return "(u " + ((char)u) + ")";
        case C_STRING:
            return "(cstr " + addr + ")";
        case UNICODE_STRING:
            return "(cstr " + addr + ")";
        default:
            return "(ref " + addr +")";
        }
    }
  }

}
