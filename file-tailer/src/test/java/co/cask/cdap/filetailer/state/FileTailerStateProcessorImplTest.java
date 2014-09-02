/*
 * Copyright 2014 Cask, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.filetailer.state;

import co.cask.cdap.filetailer.state.exception.FileTailerStateProcessorException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class FileTailerStateProcessorImplTest {

  @Test
  public void saveLoadStateTest() throws FileTailerStateProcessorException {
    FileTailerStateProcessor stateProcessor =
      new FileTailerStateProcessorImpl(System.getProperty("user.home") + "/ft_state_dir", "ft.state");

    FileTailerState state = new FileTailerState("name", 101, "hash".hashCode(), 102);

    File file = new File(System.getProperty("user.home") + "/ft_state_dir/ft.state");

    stateProcessor.saveState(state);

    Assert.assertTrue(file.exists());

    FileTailerState loadedState = stateProcessor.loadState();

    Assert.assertEquals(state.getFileName(), loadedState.getFileName());
    Assert.assertEquals(state.getPosition(), loadedState.getPosition());
    Assert.assertEquals(state.getHash(), loadedState.getHash());
    Assert.assertEquals(state.getLastModifyTime(), loadedState.getLastModifyTime());

    new File(System.getProperty("user.home") + "/ft_state_dir/ft.state").delete();
    new File(System.getProperty("user.home") + "/ft_state_dir").delete();
  }
}
