import { Box, Flex, Heading, Text } from '@chakra-ui/react';
import { React } from 'react';
import ActiveJob from '../components/ActiveJob';
import { useJobs } from '../hooks/useJobs';

function displayActiveJobs(jobs, emptyMessage = 'No active jobs currently') {
  jobs = jobs.filter(job => !job.completed);

  if (jobs.length > 0) {
    return (
      <Flex wrap="wrap" rowGap="3em" gap="7%">
        {jobs.map(job => (
          <Box minW="20em">
            <ActiveJob job={job} key={job.jobId.toString()} />
          </Box>
        ))}
      </Flex>
    );
  } else {
    return (
      <Text ml="1em" color="gray.500">
        {emptyMessage}
      </Text>
    );
  }
}

function ActiveJobs() {
  const [batchJobs, streamJobs] = useJobs();

  return (
    <Box p="3em 7% 5em 10.5%">
      <Heading as="h4" size="sm" pb="0.75em" ml="-1em">
        Active Stream Jobs
      </Heading>
      {streamJobs && displayActiveJobs(streamJobs)}
      <Heading as="h4" size="sm" pb="0.75em" ml="-1em" mt="4em">
        Batch Jobs
      </Heading>
      {batchJobs && displayActiveJobs(batchJobs)}
    </Box>
  );
}

export default ActiveJobs;
