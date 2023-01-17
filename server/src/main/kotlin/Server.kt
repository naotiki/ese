import java.net.ServerSocket

class Server(val serverSocket:ServerSocket) : Thread(){
    override fun run(){
        val socket=serverSocket.accept()
        val serverInput=socket.getInputStream()
        val serverOutput=socket.getOutputStream()

        while (!isInterrupted){
            if (serverInput.available()!=0){

            }
        }
    }

    override fun interrupt() {
        serverSocket.close()
        super.interrupt()
    }
}




sealed class Protocol(val code:Byte){
    object Ping:Protocol(code = 0x01)
}