package mmz.pocketpc.util

class Chroot {
    class Shell {
        //private external fun fakechroot_init()

        init {
            System.loadLibrary("fakeroot")
            println("Fakeroot loaded!")
            System.loadLibrary("fakechroot")
            println("FakeChroot loaded!")
            //fakechroot_init()
            //println("FakeChroot initiated!")
        }
    }
}