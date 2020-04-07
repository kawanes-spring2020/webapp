package com.CSYE6225.shubham.CloudComputing.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONStringer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.CSYE6225.shubham.CloudComputing.model.Bill;
import com.CSYE6225.shubham.CloudComputing.model.BillReturn;
import com.CSYE6225.shubham.CloudComputing.model.File;
import com.CSYE6225.shubham.CloudComputing.model.FileReturn;
import com.CSYE6225.shubham.CloudComputing.model.User;
import com.CSYE6225.shubham.CloudComputing.model.UserReturn;
import com.CSYE6225.shubham.CloudComputing.repository.BillRepository;
import com.CSYE6225.shubham.CloudComputing.repository.FileRepository;
import com.CSYE6225.shubham.CloudComputing.repository.UserRepository;
import com.CSYE6225.shubham.CloudComputing.service.AmazonClient;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.StopWatch;

@RestController
@RequestMapping("/v2")
public class UserController implements Runnable {
	private static String UPLOADED_FOLDER = System.getProperty("user.dir")+"/assets/";
	private Gson gson = new Gson();
	private static final Logger LOGGER=LoggerFactory.getLogger(UserController.class);

	private AmazonClient amazonClient;
	@Autowired
        private StatsDClient statsDclient;
        
    	@Autowired
    	UserController(AmazonClient amazonClient,BillRepository billrepository) {
		this.amazonClient = amazonClient;
		this.billrepository = billrepository;
    	}

	@Autowired
	UserRepository repository;

	@Autowired
	BillRepository billrepository;

	@Autowired
	FileRepository filerepository;

	@Autowired
	PasswordEncoder encoder;
	
	@GetMapping("/test")
	public ResponseEntity<String> testUser() {
		
			LOGGER.info("Logging in test user method");
			statsDclient.recordExecutionTimeToNow("Execution time test", System.currentTimeMillis());
			statsDclient.incrementCounter("testUser");
			System.out.println("Test Endpoint");
			
		return ResponseEntity.status(200).body("Test User verified successfully");

	}

	@GetMapping("/user/self")
	public ResponseEntity<UserReturn> getUser(@RequestHeader HttpHeaders headers) {
		try {
			LOGGER.info("Logging in get user method");
			statsDclient.incrementCounter("getuser");
			StopWatch stopwatch = StopWatch.createStarted();
	        
			String username = "";// username commnet
			String password = "";
			User _user = null;
			final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				final String[] values = credentials.split(":", 2);
				username = values[0];
				password = values[1];
			}

			_user = repository.findByEmail(username);

			if (repository.existsByEmail(username) && encoder.matches(password, _user.getPassword())) {

				UserReturn returnUser = new UserReturn(_user.getId(), _user.getEmail(), _user.getFirst_name(),
						_user.getLast_name(), _user.getAccount_created(), _user.getAccount_updated());
				stopwatch.stop();
				statsDclient.recordExecutionTime("GetUser Method execute time", stopwatch.getTime());
				return ResponseEntity.ok().body(returnUser);

			} else {
				stopwatch.stop();
				statsDclient.recordExecutionTime("GetUser Method execute time", stopwatch.getTime());
				return ResponseEntity.status(401).build();
			}
			

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(null);
		}

	}

	@PostMapping(value = "/user")
	public ResponseEntity<UserReturn> postUser(@RequestBody User user) {
		LOGGER.info("Logging in post user method");
		statsDclient.incrementCounter("postuser");
		StopWatch stopwatch = StopWatch.createStarted();
        
		String regex = "^(.+)@(.+)$";

		Pattern pattern = Pattern.compile(regex);

		if (!user.getEmail().matches(regex)) {
			System.out.println("email" + user.getEmail().matches(regex));
			return ResponseEntity.badRequest().body(null);
		}
		if (repository.existsByEmail(user.getEmail())) {
			System.out.println("email exist" + repository.existsByEmail(user.getEmail()));
			return ResponseEntity.badRequest().body(null);
		}
		String specialChars = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{8,40})";
		if (!user.getPassword().matches(specialChars)) {
			System.out.println("PWD");
			return ResponseEntity.badRequest().body(null);
		}
		User _user = repository.save(new User(user.getEmail(), encoder.encode(user.getPassword()), user.getFirst_name(),
				user.getLast_name(), "", ""));
		UserReturn returnUser = new UserReturn(_user.getId(), _user.getEmail(), _user.getFirst_name(),
				_user.getLast_name(), _user.getAccount_created(), _user.getAccount_updated());
		System.out.println("reached" + returnUser);
		stopwatch.stop();
		statsDclient.recordExecutionTime("Post User Method execute time", stopwatch.getTime());
		return ResponseEntity.ok().body(returnUser);
	}

	@PutMapping(value = "/user/self")
	public ResponseEntity<String> updateUser(@RequestBody User user, @RequestHeader HttpHeaders headers) {
		LOGGER.info("Logging in update user method test");
		statsDclient.incrementCounter("UpdateUser");
		StopWatch stopwatch = StopWatch.createStarted();
		String username = "";
		String password = "";
		User _user = null;
		final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
			String base64Credentials = authorization.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			final String[] values = credentials.split(":", 2);
			System.out.println(values[0] + "" + values[1]);
			username = values[0];
			password = values[1];
		}
		_user = repository.findByEmail(username);
		if (_user != null && encoder.matches(password, _user.getPassword())) {
			if (repository.existsByEmail(user.getEmail())) {

//	            return ResponseEntity.badRequest().body("Existing Email Address");

				String specialChars = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{8,40})";
				if (!user.getPassword().matches(specialChars)) {
					return ResponseEntity.badRequest().body("Please enter Strong password");
				}

//	        _user.setEmail(user.getEmail());
				_user.setPassword(encoder.encode(user.getPassword()));
				_user.setFirst_name(user.getFirst_name());
				_user.setLast_name(user.getLast_name());
				_user.setAccount_updated(LocalDateTime.now().toString());
				
				stopwatch.stop();
				statsDclient.recordExecutionTime("Update User Method execute time", stopwatch.getTime());
				repository.save(_user);

				return ResponseEntity.noContent().build();
			} else {
				return ResponseEntity.badRequest().body("Email cannot be updated");
			}
		} else {
			return ResponseEntity.status(401).body("Please enter correct credentials");
		}

	}

	// --------------------------------------------------------------------------------------------------------------------------------------

	@PostMapping(value = "/bill/")
	public ResponseEntity<BillReturn> createBill(@RequestBody Bill bill, @RequestHeader HttpHeaders headers) {
		try {
			LOGGER.info("Logging in create bill method method test");
			statsDclient.incrementCounter("createbill");
			StopWatch stopwatch = StopWatch.createStarted();
			String username = "";
			String password = "";
			UUID owner_id = null;
			User _user = null;
			final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				final String[] values = credentials.split(":", 2);
				System.out.println(values[0] + "" + values[1]);
				username = values[0];
				password = values[1];
			}
			_user = repository.findByEmail(username);
			if (_user != null && encoder.matches(password, _user.getPassword())) {
				owner_id = _user.getId();
				Bill billvar = billrepository.save(new Bill("", "", owner_id, bill.getVendor(), bill.getBill_date(),
						bill.getDue_date(), bill.getAmount_due(), bill.getPayment_status(), bill.getCategories(),
						bill.getAttachment()));

				BillReturn billreturn = new BillReturn(billvar.getId(), billvar.getCreated_ts(),
						billvar.getUpdated_ts(), owner_id, bill.getVendor(), bill.getBill_date(), bill.getDue_date(),
						bill.getAmount_due(), bill.getPayment_status(), bill.getCategories(),
						gson.fromJson(billvar.getAttachment(), FileReturn.class));
				stopwatch.stop();
				statsDclient.recordExecutionTime("Create Bill Method execute time", stopwatch.getTime());
				return ResponseEntity.status(201).body(billreturn);
			}
		} catch (Exception e) {
			
			return ResponseEntity.badRequest().body(null);
		}
		
		return ResponseEntity.badRequest().body(null);
	}

	@GetMapping(value = "/bills")
	public ResponseEntity<List<BillReturn>> getAllBills(@RequestHeader HttpHeaders headers) {
		try {
			LOGGER.info("Logging in get bills method method test");
			statsDclient.incrementCounter("getAllBills");
			StopWatch stopwatch = StopWatch.createStarted();
			String username = "";
			String password = "";
			UUID owner_id = null;
			User _user = null;
			List<Bill> tempBill = new ArrayList<Bill>();
			List<BillReturn> returnList = new ArrayList<BillReturn>();
			final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				final String[] values = credentials.split(":", 2);
				System.out.println(values[0] + "" + values[1]);
				username = values[0];
				password = values[1];
			}
			_user = repository.findByEmail(username);
			if (_user != null && encoder.matches(password, _user.getPassword())) {
				owner_id = _user.getId();
				tempBill = billrepository.findByOwner(owner_id);
				for (Bill billvar : tempBill) {
					System.out.println(billvar.getAttachment());

					BillReturn billreturn = new BillReturn(billvar.getId(), billvar.getCreated_ts(),
							billvar.getUpdated_ts(), owner_id, billvar.getVendor(), billvar.getBill_date(),
							billvar.getDue_date(), billvar.getAmount_due(), billvar.getPayment_status(),
							billvar.getCategories(), gson.fromJson(billvar.getAttachment(), FileReturn.class));

					returnList.add(billreturn);
				}
				stopwatch.stop();
				statsDclient.recordExecutionTime("Get All Bills Method execute time", stopwatch.getTime());
				return ResponseEntity.ok().body(returnList);
			} else {
				return ResponseEntity.status(401).build();
			}

		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.badRequest().body(null);
		}
	}

	@GetMapping(value = "/bills/due/{noOfDays}")
	public ResponseEntity<String> getBillsDue(@PathVariable(value = "noOfDays") int noOfDays,@RequestHeader HttpHeaders headers) {
		try {
			System.out.println(noOfDays);
			LOGGER.info("Logging in get all due bills method method test");
			statsDclient.incrementCounter("getAllBillsDue");
			StopWatch stopwatch = StopWatch.createStarted();
			String username = "";
			String password = "";
			UUID owner_id = null;
			User _user = null;
			List<Bill> tempBill = new ArrayList<Bill>();
			List<BillReturn> returnList = new ArrayList<BillReturn>();
			final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				final String[] values = credentials.split(":", 2);
				System.out.println(values[0] + "" + values[1]);
				username = values[0];
				password = values[1];
			}
			_user = repository.findByEmail(username);
			if (_user != null && encoder.matches(password, _user.getPassword())) {
				this.amazonClient.sendMessage(username, noOfDays,_user.getId());
				stopwatch.stop();
				statsDclient.recordExecutionTime("Get All Bills Method execute time", stopwatch.getTime());
				Thread managerthread = new Thread(new UserController(this.amazonClient,this.billrepository),"T2");
		        managerthread.start();

				return ResponseEntity.ok().body("Request Processing!! You will receive an email with response.");
			} else {
				return ResponseEntity.status(401).build();
			}

		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.badRequest().body(null);
		}
	}
	
	 @Override
		public void run() {
		 	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		 	List<Bill> tempBill = new ArrayList<Bill>();
		 	List<String> returnList = new ArrayList<String>();
			String output = this.amazonClient.receiveAndDelete();
			String email = output.split(":")[0];
			int days = Integer.parseInt(output.split(":")[1]);
			UUID user_id = UUID.fromString((output.split(":")[2]));
			tempBill = billrepository.findByOwner(user_id);
			System.out.println(tempBill);
			Date required = DateUtils.addDays(new Date(), days);
//			long DAY_IN_MS = 1000 * 60 * 60 * 24;
//			Date required = new Date(System.currentTimeMillis() + (days * DAY_IN_MS));
			String vala = null;
			Date to = new Date();
			Date today = null;
			String requiredToday = sdf.format(to);
			vala = sdf.format(required);
			System.out.println(vala);
			Date requiredDate = null;
			try {
				requiredDate = sdf.parse(vala);
				today = sdf.parse(requiredToday);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(requiredDate);
			
			
			for (Bill billvar : tempBill) {
				String val = "";
				String due = billvar.getDue_date();
				Date dueDate = null;
				try {
					dueDate = sdf.parse(due);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				BillReturn billreturn = new BillReturn(billvar.getId(), billvar.getCreated_ts(),
						billvar.getUpdated_ts(), user_id, billvar.getVendor(), billvar.getBill_date(),
						billvar.getDue_date(), billvar.getAmount_due(), billvar.getPayment_status(),
						billvar.getCategories(), gson.fromJson(billvar.getAttachment(), FileReturn.class));
				val = "http://prod.shubhamkawane.me/v2/bill/"+billvar.getId();
				if(dueDate.compareTo(today)> 0) {
					if(DateUtils.isSameDay(requiredDate, dueDate) || requiredDate.after(dueDate)) {
						returnList.add(val);
					}
				}
				
				
			}
			returnList.add(email);
			
			this.amazonClient.publishSNSMessage(returnList.toString());
		}


	@GetMapping(value = "/bill/{id}")
	public ResponseEntity<List<BillReturn>> getBill(@PathVariable(value = "id") UUID id,
			@RequestHeader HttpHeaders headers) throws JSONException {
		LOGGER.info("Logging in get single bill method method test");
		statsDclient.incrementCounter("GetSIngleBill");
		StopWatch stopwatch = StopWatch.createStarted();
		Gson gson = new Gson();
		String username = "";
		String password = "";
		UUID owner_id = null;
		User _user = null;
		Boolean flag = false;
		List<Bill> tempBill = new ArrayList<Bill>();
		List<BillReturn> returnList = new ArrayList<BillReturn>();
		final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
			String base64Credentials = authorization.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			final String[] values = credentials.split(":", 2);
			System.out.println(values[0] + "" + values[1]);
			username = values[0];
			password = values[1];
		}
		_user = repository.findByEmail(username);
		if (_user != null && encoder.matches(password, _user.getPassword())) {
			Bill availableBill = billrepository.findById(id);
			owner_id = _user.getId();
			tempBill = billrepository.findByOwner(owner_id);
			if (availableBill != null) {
				for (Bill billvar : tempBill) {
					if (billvar.getId().equals(availableBill.getId())) {
						flag = true;
						BillReturn billreturn = new BillReturn(billvar.getId(), billvar.getCreated_ts(),
								billvar.getUpdated_ts(), owner_id, billvar.getVendor(), billvar.getBill_date(),
								billvar.getDue_date(), billvar.getAmount_due(), billvar.getPayment_status(),
								billvar.getCategories(), gson.fromJson(billvar.getAttachment(), FileReturn.class));
						stopwatch.stop();
						statsDclient.recordExecutionTime("Get Bill By ID Method execute time", stopwatch.getTime());
						returnList.add(billreturn);
					}
				}
			}
			if (availableBill == null || flag == false) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok().body(returnList);
		}

		return ResponseEntity.status(401).build();
	}

	@PutMapping(value = "/bill/{id}")
	public ResponseEntity<BillReturn> updateBill(@PathVariable(value = "id") UUID id, @RequestBody Bill bill,
			@RequestHeader HttpHeaders headers) throws JSONException {
		LOGGER.info("Logging in update bill method test");
		statsDclient.incrementCounter("UpdateBill");
		StopWatch stopwatch = StopWatch.createStarted();
		String username = "";
		String password = "";
		UUID owner_id = null;
		User _user = null;
		Boolean flag = false;
		List<Bill> tempBill = new ArrayList<Bill>();
		List<BillReturn> returnList = new ArrayList<BillReturn>();
		final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
			String base64Credentials = authorization.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			final String[] values = credentials.split(":", 2);
			System.out.println(values[0] + "" + values[1]);
			username = values[0];
			password = values[1];
		}
		_user = repository.findByEmail(username);
		if (_user != null && encoder.matches(password, _user.getPassword())) {
			Bill availableBill = billrepository.findById(id);
			owner_id = _user.getId();
			tempBill = billrepository.findByOwner(owner_id);
			if (availableBill != null) {
				for (Bill billvar : tempBill) {
					if (billvar.getId().equals(availableBill.getId())) {
						flag = true;
						availableBill.setVendor(bill.getVendor());
						availableBill.setBill_date(bill.getBill_date());
						availableBill.setDue_date(bill.getDue_date());
						availableBill.setAmount_due(bill.getAmount_due());
						availableBill.setCategories(bill.getCategories());
						availableBill.setPayment_status(bill.getPayment_status());
						availableBill.setUpdated_ts(LocalDateTime.now().toString());
						Bill newBill = billrepository.save(availableBill);
						BillReturn billreturn = new BillReturn(newBill.getId(), newBill.getCreated_ts(),
								newBill.getUpdated_ts(), owner_id, newBill.getVendor(), newBill.getBill_date(),
								newBill.getDue_date(), newBill.getAmount_due(), newBill.getPayment_status(),
								newBill.getCategories(), gson.fromJson(billvar.getAttachment(), FileReturn.class));
						stopwatch.stop();
						statsDclient.recordExecutionTime("Update Bill Method execute time", stopwatch.getTime());
						return ResponseEntity.ok().body(billreturn);
					}
				}
			}
			if (availableBill == null || flag == false) {
				return ResponseEntity.notFound().build();
			}

		} else {
			return ResponseEntity.status(401).build();
		}
		return null;

	}

	@DeleteMapping(value = "/bill/{id}")
	public ResponseEntity<BillReturn> deleteBill(@PathVariable(value = "id") UUID id,
			@RequestHeader HttpHeaders headers) {
		try {
			LOGGER.info("Logging in delete bill method method test");
			statsDclient.incrementCounter("deleteBill");
			StopWatch stopwatch = StopWatch.createStarted();
			String username = "";
			String password = "";
			UUID owner_id = null;
			User _user = null;
			Boolean flag = false;
			List<Bill> tempBill = new ArrayList<Bill>();
			List<BillReturn> returnList = new ArrayList<BillReturn>();
			final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				final String[] values = credentials.split(":", 2);
				System.out.println(values[0] + "" + values[1]);
				username = values[0];
				password = values[1];
			}
			_user = repository.findByEmail(username);
			if (_user != null && encoder.matches(password, _user.getPassword())) {
				Bill availableBill = billrepository.findById(id);
				owner_id = _user.getId();
				tempBill = billrepository.findByOwner(owner_id);
				if (availableBill != null) {
					for (Bill billvar : tempBill) {
						if (billvar.getId().equals(availableBill.getId())) {
							flag = true;
							billrepository.delete(availableBill);
							File filevar = gson.fromJson(billvar.getAttachment(), File.class);
							this.amazonClient.deleteFileFromS3Bucket(filevar.getUrl());
							filerepository.delete(filevar);
							java.io.File fileio = new java.io.File(filevar.getUrl());
							fileio.delete();
							stopwatch.stop();
							statsDclient.recordExecutionTime("Delete Bill Method execute time", stopwatch.getTime());
							return ResponseEntity.status(204).build();
						}
					}
				}
				if (availableBill == null || flag == false) {
					return ResponseEntity.notFound().build();
				}

			} else {
				return ResponseEntity.status(401).build();
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(null);
		}
		return null;

	}
//---------------------------------------------------------------------------------------------------------

	@PostMapping(value = "/bill/{id}/file")
	public ResponseEntity<FileReturn> singleFileUpload(@RequestParam("file") MultipartFile file,
			@PathVariable(value = "id") UUID id, @RequestHeader HttpHeaders headers) {

		try {
			LOGGER.info("Logging in create bill attachment method test");
			statsDclient.incrementCounter("createBillAttachment");
			StopWatch stopwatch = StopWatch.createStarted();
			String username = "";
			String password = "";
			UUID owner_id = null;
			User _user = null;
			Boolean flag = false;
			List<Bill> tempBill = new ArrayList<Bill>();
			List<BillReturn> returnList = new ArrayList<BillReturn>();
			final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				final String[] values = credentials.split(":", 2);
				System.out.println(values[0] + "" + values[1]);
				username = values[0];
				password = values[1];
			}
			_user = repository.findByEmail(username);
			if (_user != null && encoder.matches(password, _user.getPassword())) {
				Bill availableBill = billrepository.findById(id);
				owner_id = _user.getId();
				tempBill = billrepository.findByOwner(owner_id);
				if (availableBill != null) {
					for (Bill billvar : tempBill) {
						if (billvar.getId().equals(availableBill.getId())) {
							flag = true;
							if (file.isEmpty()) {
								return ResponseEntity.status(200).build();
							}
							File fil = gson.fromJson(billvar.getAttachment(), File.class);
							if(fil.getId()!=null) {
								return ResponseEntity.status(201).build();
							}
							Gson gson = new Gson();
							byte[] bytes = file.getBytes();
							Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
							java.io.File f = new java.io.File(path.toString());
							Boolean exist = f.exists();
							int length = (int) f.length();
							JSONObject attachment = new JSONObject();
							if(!exist) {
								Files.write(path, bytes);
								String url = this.amazonClient.uploadFile(file);
								String checksum = DigestUtils.md5DigestAsHex(new FileInputStream(path.toString()));
								File filevar = filerepository
										.save(new File("", file.getOriginalFilename(), url,checksum,length));
								attachment.put("file_name", file.getOriginalFilename());
								attachment.put("id", filevar.getId());
								attachment.put("url", path.toString());
								attachment.put("upload_date", filevar.getUpload_date());
								FileReturn filereturn = new FileReturn(filevar.getId(), filevar.getUpload_date(),
										filevar.getFile_name(), filevar.getUrl());
								String jsonInString = gson.toJson(filevar, File.class);
								availableBill.setAttachment(jsonInString);
								billrepository.save(availableBill);
								stopwatch.stop();
								statsDclient.recordExecutionTime("Create Bill attachment Method execute time", stopwatch.getTime());
								return ResponseEntity.status(201).body(filereturn);
							}
							else {
								Path path1 = Paths.get(UPLOADED_FOLDER+billvar.getVendor()+ file.getOriginalFilename());
								String url = this.amazonClient.uploadFile(file);
								Files.write(path1, bytes);
								String checksum = DigestUtils.md5DigestAsHex(new FileInputStream(path1.toString()));
								File filevar = filerepository
										.save(new File("", file.getOriginalFilename(), url,checksum,length));
								attachment.put("file_name", file.getOriginalFilename());
								attachment.put("id", filevar.getId());
								attachment.put("url", path1.toString());
								attachment.put("upload_date", filevar.getUpload_date());
								FileReturn filereturn = new FileReturn(filevar.getId(), filevar.getUpload_date(),
										filevar.getFile_name(), filevar.getUrl());
								String jsonInString = gson.toJson(filevar, File.class);
								availableBill.setAttachment(jsonInString);
								billrepository.save(availableBill);
								stopwatch.stop();
								statsDclient.recordExecutionTime("Create Bill attachment Method execute time", stopwatch.getTime());
								return ResponseEntity.status(201).body(filereturn);
							}
							
							

						}
					}
				}
				if (availableBill == null || flag == false) {
					return ResponseEntity.notFound().build();
				}

			} else {
				return ResponseEntity.status(401).build();
			}
		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.badRequest().body(null);
		}

		return null;
	}

	@GetMapping(value = "/bill/{id}/file/{fileid}")
	public ResponseEntity<FileReturn> getFile(@PathVariable(value = "id") UUID id,
			@PathVariable(value = "fileid") UUID fileid, @RequestHeader HttpHeaders headers) {

		try {
			LOGGER.info("Logging in Get bill attachment method test");
			statsDclient.incrementCounter("GetBillAttachment");
			StopWatch stopwatch = StopWatch.createStarted();
			String username = "";
			String password = "";
			UUID owner_id = null;
			User _user = null;
			Boolean flag = false;
			List<Bill> tempBill = new ArrayList<Bill>();
			List<BillReturn> returnList = new ArrayList<BillReturn>();
			final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				final String[] values = credentials.split(":", 2);
				System.out.println(values[0] + "" + values[1]);
				username = values[0];
				password = values[1];
			}
			_user = repository.findByEmail(username);
			if (_user != null && encoder.matches(password, _user.getPassword())) {
				Bill availableBill = billrepository.findById(id);
				owner_id = _user.getId();
				tempBill = billrepository.findByOwner(owner_id);
				if (availableBill != null) {
					for (Bill billvar : tempBill) {
						if (billvar.getId().equals(availableBill.getId())) {
							flag = true;

							File filevar = gson.fromJson(billvar.getAttachment(), File.class);
							if (fileid.equals(filevar.getId())) {
								FileReturn filereturn = new FileReturn(filevar.getId(), filevar.getUpload_date(),
										filevar.getFile_name(), filevar.getUrl());
								stopwatch.stop();
								statsDclient.recordExecutionTime("Get Bill attachment Method execute time", stopwatch.getTime());
								return ResponseEntity.status(200).body(filereturn);

							}
							else {
								return ResponseEntity.notFound().build();
							}
							
						}
					}
				}
				if (availableBill == null || flag == false) {
					return ResponseEntity.notFound().build();
				}

			} else {
				return ResponseEntity.status(401).build();
			}
		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.badRequest().body(null);
		}

		return null;
	}
	
	@DeleteMapping(value = "/bill/{id}/file/{fileid}")
	public ResponseEntity<FileReturn> deleteFileUpload(@PathVariable(value = "id") UUID id,
			@PathVariable(value = "fileid") UUID fileid, @RequestHeader HttpHeaders headers) {

		try {
			LOGGER.info("Logging in delete bill attachment method test");
			statsDclient.incrementCounter("DeleteBillAttachment");
			StopWatch stopwatch = StopWatch.createStarted();
			String username = "";
			String password = "";
			UUID owner_id = null;
			User _user = null;
			Boolean flag = false;
			List<Bill> tempBill = new ArrayList<Bill>();
			List<BillReturn> returnList = new ArrayList<BillReturn>();
			final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				final String[] values = credentials.split(":", 2);
				System.out.println(values[0] + "" + values[1]);
				username = values[0];
				password = values[1];
			}
			_user = repository.findByEmail(username);
			if (_user != null && encoder.matches(password, _user.getPassword())) {
				Bill availableBill = billrepository.findById(id);
				owner_id = _user.getId();
				tempBill = billrepository.findByOwner(owner_id);
				if (availableBill != null) {
					for (Bill billvar : tempBill) {
						if (billvar.getId().equals(availableBill.getId())) {
							flag = true;

							File filevar = gson.fromJson(billvar.getAttachment(), File.class);
							if (fileid.equals(filevar.getId())) {
								this.amazonClient.deleteFileFromS3Bucket(filevar.getUrl());
								filerepository.delete(filevar);
								billvar.setAttachment("{}");
								billrepository.save(billvar);
								java.io.File fileio = new java.io.File(filevar.getUrl());
								fileio.delete();
								stopwatch.stop();
								statsDclient.recordExecutionTime("Delete Bill attachment Method execute time", stopwatch.getTime());
								return ResponseEntity.status(204).body(null);

							}
							else {
								return ResponseEntity.notFound().build();
							}
							
						}
					}
				}
				if (availableBill == null || flag == false) {
					return ResponseEntity.notFound().build();
				}

			} else {
				return ResponseEntity.status(401).build();
			}
		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.badRequest().body(null);
		}

		return null;
	}

}
