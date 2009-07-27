package pl.touk.proxygenerator;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.touk.proxygenerator.wsdlmap.WsdlMapFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		try
		{
			WsdlMapFactory instance = new WsdlMapFactory();

			Map result = instance.createWsdlMap("/home/pnw/wsdl/przykladowy_proces/przykladowy_proces/");
			Iterator it = result.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry pairs = (Map.Entry) it.next();
				System.out.println(pairs.getKey() + " = " + pairs.getValue());
			}
		}
		catch (Exception ex)
		{
			Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
		}
    }
}
