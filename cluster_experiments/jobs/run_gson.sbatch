#!/bin/bash

################################################################################################
### sbatch configuration parameters must start with #SBATCH and must precede any other commands.
### To ignore, just add another # - like so: ##SBATCH
################################################################################################

#SBATCH --partition main			### specify partition name where to run a job. main: all nodes;
#SBATCH --time 7-00:00:00			### limit the time of job running. Make sure it is not greater than the partition time limit!! Format: D-H:MM:SS
#SBATCH --job-name evogpt-gson				### name of the job
#SBATCH --cpus-per-task=4
#SBATCH --output slurm-%J.out			### output log for running job - %J for job number
#SBATCH --error slurm-error-%J.out			### output log for running job - %J for job number
# #SBATCH --dependency=

# Note: the following 4 lines are commented out
##SBATCH --mail-type=ALL			### conditions for sending the email. ALL,BEGIN,END,FAIL, REQUEU, NONE
#SBATCH --mem=16G				### amount of RAM memory, allocating more than 60G requires IT team's permission
#SBATCH --tmp=50G        ### amount of temporary storage space, used for node's local storage


################  Following lines will be executed by a compute node    #######################

### Print some data to output file ###
echo `date`

echo "Job name is: evogpt-gson"



echo "/tmp/evogpt_gson_${SLURM_JOB_ID}"
mkdir /tmp/evogpt_gson_${SLURM_JOB_ID}/logs
chmod -R 777 /tmp/evogpt_gson_${SLURM_JOB_ID}/logs

mkdir /tmp/evogpt_gson_${SLURM_JOB_ID}/trajectories
chmod -R 777 /tmp/evogpt_gson_${SLURM_JOB_ID}/trajectories

### Start your code below ####
module load anaconda				### load anaconda module (must be present when working with conda environments)
source activate evogpt				### activate a conda environment, replace my_env with your conda environment
cd ${SLURM_SUBMIT_DIR} || exit 1				### change to project root directory
python main.py	--config cluster_experiments/jobs/configs/config_gson.json			### this command executes python script – replace with your own command

cp -r /tmp/evogpt_gson_${SLURM_JOB_ID}/logs/* cluster_experiments/jobs/logs/gson		### copy logs to the logs directory


