package org.eclipse.vorto.repository.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.vorto.repository.internal.service.JcrModelRepository;
import org.eclipse.vorto.repository.internal.service.utils.BulkUploadHelper;
import org.eclipse.vorto.repository.internal.service.utils.ModelSearchUtil;
import org.eclipse.vorto.repository.model.UploadModelResult;
import org.eclipse.vorto.repository.notification.INotificationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modeshape.jcr.SingleUseAbstractTest;
import org.springframework.core.io.ClassPathResource;

public class BulkUploadTest extends SingleUseAbstractTest  {
	
	@InjectMocks
	private JcrModelRepository modelRepository;
	@Mock
	private INotificationService notificationService;
	@Mock
	private UserRepository userRepository;
	@InjectMocks
	private ModelSearchUtil modelSearchUtil = new ModelSearchUtil();
	
	private BulkUploadHelper bulkUploadHelper;
	
	@Before
	public void beforeEach() throws Exception {
		super.beforeEach();
		startRepositoryWithConfiguration(new ClassPathResource("vorto-repository.json").getInputStream());

		modelRepository = new JcrModelRepository();
		modelRepository.setModelSearchUtil(modelSearchUtil);
		modelRepository.setSession(session());
		
		bulkUploadHelper = new BulkUploadHelper(this.modelRepository);
	}
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testUploadValidModels() throws IOException {
		String fileName = "sample_models/valid-models.zip";
		List<UploadModelResult> uploadResults = bulkUploadHelper.uploadMultiple(fromClasspath(fileName));
		assertEquals(3, uploadResults.size());
		verifyAllModelsAreValid(uploadResults);
	}

	@Test
	public void testUploadOneMissingModels() throws IOException {
		String fileName = "sample_models/missing-models.zip";
		List<UploadModelResult> uploadResults = bulkUploadHelper.uploadMultiple(fromClasspath(fileName));
		assertEquals(2, uploadResults.size());
		verifyOneModelAreMissing(uploadResults);
	}
	
	@Test
	public void testUploadInvalidModels() throws IOException {
		String fileName = "sample_models/invalid-models.zip";
		List<UploadModelResult> result = bulkUploadHelper.uploadMultiple(fromClasspath(fileName));
		assertEquals(2,result.size());
		assertFalse(result.get(0).isValid());
		assertFalse(result.get(1).isValid()); 
	}
	
	@Test
	public void testUploadDifferentModelTypesWithSameId() throws Exception {
		String fileName = "sample_models/modelsWithSameId.zip";
		List<UploadModelResult> result = bulkUploadHelper.uploadMultiple(fromClasspath(fileName));
		assertEquals(2,result.size());
		assertFalse(result.get(1).isValid()); 	
	}
	
	@Test
	public void testUploadModelWithInvalidGrammar() throws Exception {
		String fileName = "sample_models/modelsWithWrongGrammar.zip";
		List<UploadModelResult> result = bulkUploadHelper.uploadMultiple(fromClasspath(fileName));
		assertEquals(2,result.size());
		assertFalse(result.get(0).isValid());
		assertFalse(result.get(1).isValid()); 
		assertEquals("org.eclipse.vorto.examples",result.get(0).getModelResource().getId().getNamespace());
		assertEquals("Accelerometer",result.get(0).getModelResource().getId().getName());
		assertEquals("0.0.1",result.get(0).getModelResource().getId().getVersion());
	}
	
	private void verifyOneModelAreMissing(List<UploadModelResult> uploadResults) {
		assertEquals(false, uploadResults.stream().allMatch(result -> result.isValid()));
		assertEquals((uploadResults.size() - 1), uploadResults.stream().filter(result -> result.getErrorMessage() == null).count());
		assertEquals(1, uploadResults.stream().filter(result -> result.getErrorMessage() !=null).count());
	}

	private void verifyAllModelsAreValid(List<UploadModelResult> uploadResults) {
		assertEquals(true, uploadResults.stream().allMatch(result -> result.isValid()));
		assertTrue(uploadResults.stream().allMatch(result -> result.getErrorMessage() == null));
		assertTrue(uploadResults.stream().allMatch(result -> result.getHandleId() != null));
	}
	
	private String fromClasspath(String fileName) throws IOException {
		return new ClassPathResource(fileName).getFile().getAbsolutePath();
	}

}
