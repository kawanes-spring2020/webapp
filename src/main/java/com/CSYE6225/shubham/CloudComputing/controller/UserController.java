package com.CSYE6225.shubham.CloudComputing.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
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

@RestController
@RequestMapping("/v1")
public class UserController {
	private static String UPLOADED_FOLDER = System.getProperty("user.dir")+"/target/assets/";
	private Gson gson = new Gson();

	private AmazonClient amazonClient;

    @Autowired
    UserController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }

	@Autowired
	UserRepository repository;

	@Autowired
	BillRepository billrepository;

	@Autowired
	FileRepository filerepository;

	@Autowired
	PasswordEncoder encoder;
	
	

	@GetMapping("/user/self")
	public ResponseEntity<UserReturn> getUser(@RequestHeader HttpHeaders headers) {
		try {
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
				return ResponseEntity.ok().body(returnUser);

			} else {
				return ResponseEntity.status(401).build();
			}

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(null);
		}

	}

	@PostMapping(value = "/user")
	public ResponseEntity<UserReturn> postUser(@RequestBody User user) {
		System.out.println("inside" + user);
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
		return ResponseEntity.ok().body(returnUser);
	}

	@PutMapping(value = "/user/self")
	public ResponseEntity<String> updateUser(@RequestBody User user, @RequestHeader HttpHeaders headers) {
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
				return ResponseEntity.status(201).body(billreturn);
			}
		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.badRequest().body(null);
		}

		return ResponseEntity.badRequest().body(null);
	}

	@GetMapping(value = "/bills")
	public ResponseEntity<List<BillReturn>> getAllBills(@RequestHeader HttpHeaders headers) {
		try {
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
				return ResponseEntity.ok().body(returnList);
			} else {
				return ResponseEntity.status(401).build();
			}

		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.badRequest().body(null);
		}
	}

	@GetMapping(value = "/bill/{id}")
	public ResponseEntity<List<BillReturn>> getBill(@PathVariable(value = "id") UUID id,
			@RequestHeader HttpHeaders headers) throws JSONException {
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
								return ResponseEntity.status(400).build();
							}
							File fil = gson.fromJson(billvar.getAttachment(), File.class);
							if(fil.getId()!=null) {
								return ResponseEntity.status(400).build();
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
