namespace AirflowDeviceSim
{
    partial class StatsForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.label1 = new System.Windows.Forms.Label();
            this.labelSent = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.labelReceived = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Microsoft Sans Serif", 10.8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(27, 41);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(108, 24);
            this.label1.TabIndex = 0;
            this.label1.Text = "Bytes Sent";
            // 
            // labelSent
            // 
            this.labelSent.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelSent.Location = new System.Drawing.Point(200, 37);
            this.labelSent.Name = "labelSent";
            this.labelSent.Size = new System.Drawing.Size(289, 37);
            this.labelSent.TabIndex = 1;
            this.labelSent.Text = "label2";
            this.labelSent.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Font = new System.Drawing.Font("Microsoft Sans Serif", 10.8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label2.Location = new System.Drawing.Point(27, 88);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(154, 24);
            this.label2.TabIndex = 0;
            this.label2.Text = "Bytes Received";
            // 
            // labelReceived
            // 
            this.labelReceived.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelReceived.Location = new System.Drawing.Point(200, 80);
            this.labelReceived.Name = "labelReceived";
            this.labelReceived.Size = new System.Drawing.Size(289, 37);
            this.labelReceived.TabIndex = 1;
            this.labelReceived.Text = "label2";
            this.labelReceived.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // StatsForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(563, 253);
            this.Controls.Add(this.labelReceived);
            this.Controls.Add(this.labelSent);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Name = "StatsForm";
            this.Text = "Stats";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label labelSent;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label labelReceived;
    }
}