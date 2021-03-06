package org.mifosplatform.billing.randomgenerator.service;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.mifosplatform.billing.randomgenerator.domain.RandomGenerator;
import org.mifosplatform.billing.randomgenerator.domain.RandomGeneratorDetails;
import org.mifosplatform.billing.randomgenerator.domain.RandomGenertatorRepository;
import org.mifosplatform.billing.randomgenerator.serialization.RandomGeneratorCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class RandomGeneratorWritePlatformServiceImpl implements
		RandomGeneratorWritePlatformService {
	int i, j, x=0 , y;
	private final PlatformSecurityContext context;
	private final RandomGenertatorRepository randomGeneratorRepository;
	private final RandomGeneratorCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final RandomGeneratorReadPlatformService randomGeneratorReadPlatformService;
	static final String alphaNumerics = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static final String numerics = "0123456789";
	static final String alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	@Autowired
	public RandomGeneratorWritePlatformServiceImpl(
			final PlatformSecurityContext context,
			final RandomGenertatorRepository randomGeneratorRepository,
			final RandomGeneratorReadPlatformService randomGeneratorReadPlatformService,
			final RandomGeneratorCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
		this.context = context;
		this.randomGeneratorRepository = randomGeneratorRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.randomGeneratorReadPlatformService = randomGeneratorReadPlatformService;

	}

	@Override
	public CommandProcessingResult createRandomGenerator(JsonCommand command) {
		try {
			context.authenticatedUser();
			 this.fromApiJsonDeserializer
					.validateForCreate(command.json());

			final RandomGenerator randomGenerator = RandomGenerator
					.fromJson(command);

			generateRandomNumbers(randomGenerator);

			this.randomGeneratorRepository.save(randomGenerator);
			return new CommandProcessingResult(randomGenerator.getId());

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void generateRandomNumbers(RandomGenerator randomGenerator) {
		


		Set myNumbers = new HashSet();
		String minSerialSeries = "";
		String maxSerialSeries = "";
		int length = Integer
				.valueOf(randomGenerator.getLength().toString());
		String Type = randomGenerator.getPinCategory();
		Long SerialNo =randomGenerator.getSerialNo();
	
		for (x = 0; x < SerialNo; x++) {
			if (x == 0) {
				minSerialSeries += "1";
				maxSerialSeries += "9";
			} else {
				minSerialSeries += "0";
				maxSerialSeries += "9";
			}
		}
		
		
		Long minNo = Long.parseLong(minSerialSeries);
		Long maxNo = Long.parseLong(maxSerialSeries);
		Long no = this.randomGeneratorReadPlatformService.retrieveMaxNo(minNo,
				maxNo);
		
		 if(no==null){
			   minSerialSeries="";
			   for (x = 0; x < SerialNo; x++) {
			    if (x == 0) {
			     minSerialSeries += "1";
			    } else {
			     minSerialSeries += "0";
			    }
			   }
			   no=Long.parseLong(minSerialSeries);
			  }
		Long quantity = randomGenerator.getQuantity();
		x = randomGenerator.getBeginWith().length();
		y = length - x;

		@SuppressWarnings("unused")
		Random rand = new Random(y);
		if (Type.equalsIgnoreCase("Alpha")) {
			for (i = 0; i < quantity; i++) {
				String name = "";
				name += randomGenerator.getBeginWith();
				for (j = 0; j < y; j++) {
					name += alphabets.charAt((int) (Math.random() * alphabets
							.length()));
				}
				for (;;) {
					if (myNumbers.add(name)) {
						String value = this.randomGeneratorReadPlatformService
								.retrieveIndividualPin(name);

						if (value == null) {
							no += 1;
							RandomGeneratorDetails randomGeneratordetails = new RandomGeneratorDetails(
									name, no);
							randomGenerator.add(randomGeneratordetails);
							break;
						} else {
							i--;
						}
					}

				}
			}
		}
		if (Type.equalsIgnoreCase("Numeric")) {
			for (i = 0; i < quantity; i++) {
				String name = "";
				name += randomGenerator.getBeginWith();
				for (j = 0; j < y; j++) {
					name += numerics.charAt((int) (Math.random() * numerics
							.length()));
				}
				for (;;) {

					String value = this.randomGeneratorReadPlatformService
							.retrieveIndividualPin(name);
					if (value == null) {
						no +=1;
						RandomGeneratorDetails randomGeneratordetails = new RandomGeneratorDetails(
								name, no);
						randomGenerator.add(randomGeneratordetails);
						break;
					} else {
						i--;
					}
				}
			}
		}
		if (Type.equalsIgnoreCase("AlphaNumeric")) {
			for (i = 0; i < quantity; i++) {
				String name = "";
				name += randomGenerator.getBeginWith();
				for (j = 0; j < y; j++) {
					name += alphaNumerics
							.charAt((int) (Math.random() * alphaNumerics
									.length()));
				}
				for (;;) {
					String value = this.randomGeneratorReadPlatformService
							.retrieveIndividualPin(name);
					if (value == null) {
						no += 1;
						RandomGeneratorDetails randomGeneratordetails = new RandomGeneratorDetails(
								name, no);
						randomGenerator.add(randomGeneratordetails);
						break;
					} else {
						i--;
					}

				}
			}
		}

	
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("batch_name")) {
			final String name = command
					.stringValueOfParameterNamed("batchName");
			throw new PlatformDataIntegrityException(
					"error.msg.code.duplicate.batchname", "A batch with name'"
							+ name + "'already exists", "displayName", name);
		}
		if (realCause.getMessage().contains("serial_no_key")) {
			throw new PlatformDataIntegrityException(
					"error.msg.code.duplicate.serial_no_key", "A serial_no_key already exists", "displayName", "serial_no");
		}

		throw new PlatformDataIntegrityException(
				"error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());
	}

}
