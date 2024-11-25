import { AggregateCreateNodesError, logger } from '@nx/devkit';
import { execGradleAsync } from './exec-gradle';
import { existsSync } from 'fs';
import { dirname, join } from 'path';

async function execNxGradlePluginTask(gradlewFile: string) {
  let createNodesBuffer: Buffer;

  // if there is no build.gradle or build.gradle.kts file, we cannot run the projectReport nor projectReportAll task
  if (
    !existsSync(join(dirname(gradlewFile), 'build.gradle')) &&
    !existsSync(join(dirname(gradlewFile), 'build.gradle.kts'))
  ) {
    logger.warn(
      `Could not find build file near ${gradlewFile}. Please run 'nx generate @nx/gradle:init' to generate the necessary tasks.`
    );
    return [];
  }

  try {
    createNodesBuffer = await execGradleAsync(gradlewFile, ['createNodes']);
  } catch (e) {
    throw new AggregateCreateNodesError(
      [
        [
          gradlewFile,
          new Error(
            `Could not run 'createNodes' task. Please run 'nx generate @nx/gradle:init' to generate the necessary tasks.`
          ),
        ],
      ],
      []
    );
  }
}
