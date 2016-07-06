# spring-utilities
Utilities library for use with Spring Projects, allowing for Managed Resources &amp; Hotswap reloading.


## Managed Resources
The majority of this library is devoted to "Managed Resources", and enabling previously compiled resources to be generated/loaded at runtime. If you're familiar with working with exclusively JSPs, you might have asked yourself at one point if redeploying every time you make a change is necessary. This library aims to reduce the need for redeployment to exclusively Controller changes. With ManagedModel, a JSON file can be used to describe SQL statement results, which the ManagedModel class translates into an object at runtime, allowing developers to edit SQL statements, change field names, or even add/remove fields without needing to redeploy (assuming their controller doesn't explicitly reference the fields in question). Moreover, proper controller programming can almost eliminate the need for any controller changes. 

## Example

``` Java

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ServletController {
	static final Logger LOG = LoggerFactory.getLogger(ServletController.class);

	private JdbcTemplate jdbcTemplate;

	private static ClassLoader cl;

	private static SqlModelManager modelManager;

	static {
		try {
		
			cl = Thread.currentThread().getContextClassLoader();
			modelManager = new SqlModelManager(cl);
			modelManager.reload();
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@Autowired
	public ServletController(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@RequestMapping("/")
	public String index(HttpServletRequest request, Model model)
			throws Exception {

		for (String res : modelManager.getResources()) {
			modelManager.invoke(res, new Object[] { "some_user_id" },
					jdbcTemplate);
			modelManager.mapTo(res, model);
		}

		model.addAttribute("hasId", model.containsAttribute("id"));

		model.addAttribute("resources", modelManager.getResources());

		return "index";
	}

	@RequestMapping("/reload")
	public String reload(HttpServletRequest request, Model model) {
		modelManager.reload();
		return "redirect:/";
	}

```

The above file allows new models to be added without changes to the controller, and propagates them to the view, allowing for live edits to the entire application without downtime. 

The following is an example of the JSON file used to create a model.

``` JSON

{
	"type": "Model",
	"name": "UserSelect",
	"statementPath" : "user_select.sql",
	"fields": [
		{
			"access": "private",
			"type": "String",
			"name": "id",
			"generateAccessors": true,
			"resultSetExtractorName": "id"
		},
		{
			"access": "private",
			"type": "String",
			"name": "firstName",
			"generateAccessors": true,
			"resultSetExtractorName": "firstName"
		},
		{
			"access": "private",
			"type": "String",
			"name": "lastName",
			"generateAccessors": true,
			"resultSetExtractorName": "lastName"
		}
	],
	"additionalDependencies": [
	]
}
```
The following is the corresponding SQL.
``` SQL

select
	id, firstName, lastName
from userdb
where id = ?
```
