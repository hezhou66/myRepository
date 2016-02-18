package test;
import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns="/asyncDemoServlet",asyncSupported=true)
public class AsyncDemoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        //Execute the business logic in sub-thread.
        final AsyncContext ctx = request.startAsync();
        System.out.println("jincheng");
        ctx.start(new Executor(ctx));
        
    }
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
 
    public class Executor implements Runnable {
        private AsyncContext ctx = null;
        public Executor(AsyncContext ctx){
            this.ctx = ctx;
        }
        public void run(){
            try {
            	System.out.println("xiancheng");
                ctx.getResponse().getOutputStream().write("aaa".getBytes());
                //wait for 5 seconds to simulate the business logic.
                Thread.sleep(5000);
                ctx.getResponse().getOutputStream().flush();
                ctx.complete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
 
}
