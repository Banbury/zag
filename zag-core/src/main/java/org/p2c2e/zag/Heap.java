package org.p2c2e.zag;

import org.p2c2e.util.FastByteBuffer;

class HeapBlock {
    int start;
    int size;
    HeapBlock prev, next;

}

public class Heap {
    static final int MINALLOC = 1024;
    HeapBlock freeList;

    HeapBlock used;

    Zag zag;

    int heapStart;

    /** Returns a list of length n*2 where n is the number 
        of used blocks; start and length are interleaved */
    public int[] getUsedBlocks() {
        int count = 0;
        HeapBlock cur = used;
        while (cur != null) {
            count += 2;
            cur = cur.next;
        }

        int[] blocks = new int[count];
        int idx = 0;
        cur = used;
        while (cur != null) {
            blocks[idx++] = cur.start;
            blocks[idx++] = cur.size;
            cur = cur.next;
        }
        return blocks;
    }

    public int getHeapStart() {
        if (isActive())
            return heapStart;
        else
            return 0;
    }
    

    public Heap(Zag zag) {
        this.zag = zag;
        heapStart = zag.memory.limit();
    }

    public Heap(Zag zag, int heapStart) {
        this.zag = zag;
        this.heapStart = heapStart;
        int memSize = zag.memory.limit();
        if (memSize > heapStart) {
            freeList = new HeapBlock();
            freeList.start = heapStart;
            freeList.size = memSize - heapStart;
        }
    }

    public boolean isActive() {
        return used != null;
    }

    public void reserveUpTo(int end) {
        int memSize = zag.memory.limit();
        if (end <= memSize) {
            return;
        }
        end = 256 + (end & 0xffffff00);
        FastByteBuffer newMemory = zag.memory.resize(end);
        if (newMemory == null) {
            throw new RuntimeException("failed to resize memory up to " + end);
        }
        zag.memory = newMemory;
        if (freeList == null) {
            freeList = new HeapBlock();
            freeList.start = heapStart;
            freeList.size = end - heapStart;
        } else {
            HeapBlock cur = used;
            int highestEnd = 0;
            while (cur != null) {
                int curend = cur.start + cur.size;
                if (curend > highestEnd) {
                    highestEnd = curend;
                }
                cur = cur.next;
            }

            cur = freeList;
            while (cur.next != null) {
                cur = cur.next;
            }
            if (cur.start > highestEnd) {
                cur.size = end - cur.start;
            } else {
                HeapBlock newBlock = new HeapBlock();
                newBlock.start = highestEnd;
                newBlock.size = end - newBlock.start;
                newBlock.prev = cur;
                cur.next = used;
            }
        }

    }

    /** assumes enough space reserved */
    public void mallocAt(int start, int size) {
        HeapBlock next = freeList;
        HeapBlock cur = null;
        while (next != null) {
            if (next.start > start) {
                break;
            }
            cur = next;
            next = next.next;
        }
        //the free block to split is at prev

        int end = start + size;
        int freeSpaceBefore = start - cur.start;
        int freeSpaceAfter = cur.start + cur.size - end;
        //exact
        if (freeSpaceBefore == 0 && freeSpaceAfter == 0) {
            if (cur.prev == null) {
                freeList = next;
            } else {
                cur.prev.next = next;
            }
            if (next != null) {
                next.prev = cur.prev;
            }
            cur.prev = null;
            cur.next = used;
            if (used != null) {
                used.prev = cur;
            }
            used = cur;
            return;
        }

        HeapBlock newBlock = new HeapBlock();
        newBlock.start = start;
        newBlock.size = size;
        newBlock.next = used;
        if (used != null) {
            used.prev = newBlock;
        }
        used = newBlock;

        if (freeSpaceBefore == 0) {
            cur.start = end;
        } else {
            if (freeSpaceAfter == 0) {
                cur.size = start - cur.start;
            } else {
                HeapBlock after = new HeapBlock();
                after.start = end;
                after.size = cur.start + cur.size - end;
                after.next = next;
                after.prev = cur;
                cur.next = after;
                if (next != null) {
                    next.prev = after;
                }
                cur.size = start - cur.start;
            }
        }

    }

    public int malloc(int size) {
        if (used == null) {
            heapStart = zag.memory.limit();
        }
        HeapBlock cur = freeList;
        int lastBlockEnd = heapStart;
        while (cur != null) {
            if (cur.size == size) {
                if (freeList == cur) {
                    freeList = cur.next;
                }
                //an exact-sized block
                if (cur.prev != null) {
                    cur.prev.next = cur.next;
                }
                if (cur.next != null) {
                    cur.next.prev = cur.prev;
                }
                cur.prev = null;
                cur.next = used;
                if (used != null) {
                    used.prev = cur;
                }
                used = cur;
                return cur.start;
            } else if (cur.size > size) {
                //need to split this block. 
                HeapBlock newFreeBlock = new HeapBlock();
                newFreeBlock.size = cur.size - size;
                newFreeBlock.start = cur.start + size;
                newFreeBlock.next = cur.next;
                newFreeBlock.prev = cur.prev;
                if (freeList == cur) {
                    freeList = cur.next;
                }
                if (cur.prev == null) {
                    freeList = newFreeBlock;
                } else {
                    cur.prev.next = newFreeBlock;
                }
                if (cur.next != null) {
                    cur.next.prev = newFreeBlock;
                }
                cur.prev = null;
                cur.next = used;
                if (used != null) {
                    used.prev = cur;
                }
                used = cur;
                cur.size = size;
                return cur.start;
            }
            lastBlockEnd = cur.start + cur.size;
            cur = cur.next;
        }
        //at end of free list, need to make a new free block
        int memSize = zag.memory.limit();
        int toAlloc = 256 + (size & 0xffffff00);
        if (toAlloc < MINALLOC) {
            toAlloc = MINALLOC;
        }
        FastByteBuffer newMemory = zag.memory.resize(memSize + toAlloc);
        if (newMemory == null) {
            return 0;
        }
        zag.memory = newMemory;
        HeapBlock newUsedBlock = new HeapBlock();
        newUsedBlock.start = lastBlockEnd;
        newUsedBlock.size = size;
        newUsedBlock.next = used;
        if (used != null) {
            used.prev = newUsedBlock;
        }
        used = newUsedBlock;
        int newEnd = newUsedBlock.start + newUsedBlock.size;
        if (newEnd < memSize + toAlloc) {
            HeapBlock newFreeBlock = new HeapBlock();
            newFreeBlock.start = newEnd;
            newFreeBlock.size = memSize + toAlloc - newFreeBlock.start;
            if (freeList == null) {
                freeList = newFreeBlock;
            } else {
                cur = freeList;
                while (cur.next != null) {
                    cur = cur.next;
                }
                cur.next = newFreeBlock;
                newFreeBlock.prev = cur;
            }
        }
        return newUsedBlock.start;
    }

    void dump(HeapBlock block) {
        HeapBlock cur = block;
        while (cur != null) {
            System.out.println("  block at " + cur.start + " len " + cur.size);
            String prev, next;
            if (cur.prev == null) {
                prev = "null prev";
            } else {
                if (cur.prev.next == cur) {
                    prev = "correct prev";
                } else {
                    prev = "wrong prev";
                }
            }
            if (cur.next == null) {
                next = "null next";
            } else {
                if (cur.next.prev == cur) {
                    next = "correct next";
                } else {
                    next = "wrong next";
                }
            }
            System.out.println("  " + prev + ", " + next);
            if (cur.next == cur) {
                System.out.println("  bad loop\n");
                return;
            }
            cur = cur.next;
        }
    }

    void dump() {
        System.out.println("DUMP used: ");
        dump (used);
        System.out.println("DUMP free: ");
        dump (freeList);
    }

    void free(int address) {
        HeapBlock cur = used;
        while (cur != null) {
            if (cur.start == address) {
                //remove this from used
                if (cur.prev == null) {
                    used = cur.next;
                } else {
                    cur.prev.next = cur.next;
                }
                if (cur.next != null) {
                    cur.next.prev = cur.prev;
                }

                //figure out where in the free list this goes
                HeapBlock nextFree = freeList;
                HeapBlock prevFree = null;
                while (nextFree != null) {
                    if (nextFree.start > address) {
                        break;
                    }
                    prevFree = nextFree;
                    nextFree = nextFree.next;
                }

                //check for coalesce with previous
                if (prevFree != null) {
                    if (prevFree.start + prevFree.size == cur.start) {
                        prevFree.size += cur.size;
                        prevFree.next = nextFree;
                        if (nextFree != null) {
                            nextFree.prev = prevFree;
                        }
                        cur = prevFree;
                    } else {
                        prevFree.next = cur;
                        cur.prev = prevFree;
                    }
                } else {
                    cur.prev = null;
                    freeList = cur;
                }

                //check for coalesce with next
                if (nextFree != null) {
                    if (cur.start + cur.size == nextFree.start) {
                        cur.size += nextFree.size;
                        cur.next = nextFree.next;
                        if (nextFree.next != null) {
                            nextFree.next.prev = cur;
                        }
                    } else {
                        cur.next = nextFree;
                        nextFree.prev = cur;
                    }
                } else {
                    cur.next = nextFree;
                }

                if (used == null) {
                    //no more used blocks, resize memory back down
                    zag.memory = zag.memory.resize(heapStart);
                    if (zag.memory == null) {
                        System.out.println("impending doom after resizing to " + heapStart);
                    }
                    freeList = null; //start clean next time
                }

                return;
            }
            cur = cur.next;
        }
        throw new RuntimeException("tried to free a bogus block at " + address);
    }

}